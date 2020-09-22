package com.bybutter.middleware.distributed.lock

import io.lettuce.core.SetArgs
import io.lettuce.core.api.StatefulRedisConnection
import org.apache.commons.logging.LogFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import org.springframework.util.ReflectionUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class RedisDistributedLock : DistributedLock {

    val ok = "OK"

    private val logger = LogFactory.getLog(RedisDistributedLock::class.java)

    private var statefulRedisConnection: StatefulRedisConnection<String, String>
    private var rKey: String
    private var rValue: String
    private var leaseTime: Long = 6000L

    private var executor: ExecutorService = Executors.newCachedThreadPool(CustomizableThreadFactory("redis-lock-registry-"))

    private var executorWatchDog: ScheduledExecutorService = Executors.newScheduledThreadPool(10)

    private var localLock = ReentrantLock()
    private var lockedAt: Long = 0L

    @Volatile
    private var unlinkAvailable = true

    private var enableWatchDog: Boolean = false
    private var threshold: Long = 2000L
    private var leaseRenewalTime: Long = 1000L
    private var leaseRenewalNumber: Int = 5

    constructor(statefulRedisConnection: StatefulRedisConnection<String, String>, rKey: String, rValue: String, leaseTime: Long, enableWatchDog: Boolean, threshold: Long, leaseRenewalTime: Long, leaseRenewalNumber: Int) {
        this.statefulRedisConnection = statefulRedisConnection
        this.rKey = "RedisDistributedLock:$rKey"
        this.rValue = rValue
        this.leaseTime = leaseTime
        this.enableWatchDog = enableWatchDog
        this.threshold = threshold
        this.leaseRenewalTime = leaseRenewalTime
        this.leaseRenewalNumber = leaseRenewalNumber
    }

    constructor(statefulRedisConnection: StatefulRedisConnection<String, String>, rKey: String, rValue: String) : this(statefulRedisConnection, rKey, rValue, 6000L, false, 0, 0, 0)

    override fun tryLock(): Boolean {
        return try {
            tryLock(0, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
    }

    fun tryLock(time: Long, unit: TimeUnit): Boolean {
        val now = System.currentTimeMillis()
        if (!this.localLock.tryLock(time, unit)) {
            return false
        }
        try {
            val expire = now + TimeUnit.MILLISECONDS.convert(time, unit)
            var acquired: Boolean
            while (redisLock().also { acquired = it }.not() && System.currentTimeMillis() < expire) {
                Thread.sleep(100)
            }
            if (!acquired) {
                this.localLock.unlock()
            }
            if (acquired) {
                enableWatchDog()
            }
            return acquired
        } catch (e: Exception) {
            this.localLock.unlock()
            rethrowAsLockException(e)
        }
        return false
    }

    override fun lock() {
        localLock.lock()
        while (true) {
            try {
                while (!redisLock()) {
                    Thread.sleep(100)
                }
                break
            } catch (e: InterruptedException) {
            } catch (e: Exception) {
                localLock.unlock()
                rethrowAsLockException(e)
            }
        }
        enableWatchDog()
    }

    private fun enableWatchDog() {
        if (enableWatchDog) {
            val times = AtomicInteger(0)
            val currentThread = Thread.currentThread()
            this.executorWatchDog.scheduleAtFixedRate({
                if (times.get() > this.leaseRenewalNumber) {
                    // This is not to throw a real exception, just to make the log print good-looking
                    RuntimeException("RedisDistributedLock's value is ${this.rValue}, leaseRenewalNumber Threshold exceeded in RedisDistributedLock.").printStackTrace()
                    // if main thread is sleep, wait or blocking,main thread can interrupt
                    currentThread.interrupt()
                    throw RuntimeException("exit watchDog.")
                }
                watchDog(threshold, leaseRenewalTime, times)
            }, 0, 100L, TimeUnit.MILLISECONDS)
        }
        logger.info("start lock, RedisDistributedLock's value is ${this.rValue}")
    }

    private fun redisLock(): Boolean {
        val result = this.statefulRedisConnection.sync().set( rKey,
                rValue,
                SetArgs.Builder.nx().px(leaseTime)
        )?:false

        if (result == ok) {
            this.lockedAt = System.currentTimeMillis()
        }
        return result == ok
    }

    override fun unLock() {
        check(localLock.isHeldByCurrentThread) { "You do not own lock at " + this.rKey }
        if (localLock.holdCount > 1) {
            localLock.unlock()
            return
        }
        try {
            check(isAcquiredInThisProcess()) {
                "Lock was released in the store due to expiration. " +
                        "The integrity of data protected by this lock may have been compromised."
            }
            if (Thread.currentThread().isInterrupted) {
                this.executor.execute { this.removeLockKey() }
            } else {
                removeLockKey()
            }
            logger.info("end lock, RedisDistributedLock's value is ${this.rValue}")
        } catch (e: Exception) {
            ReflectionUtils.rethrowRuntimeException(e)
        } finally {
            localLock.unlock()
        }
    }

    private fun isAcquiredInThisProcess(): Boolean {
        return this.rValue ==
                this.statefulRedisConnection.sync().get(this.rKey)
    }

    private fun removeLockKey() {
        if (this.unlinkAvailable) {
            try {
                this.statefulRedisConnection.sync().unlink(this.rKey)
            } catch (ex: Exception) {
                this.unlinkAvailable = false
                this.statefulRedisConnection.sync().del(this.rKey)
            }
        } else {
            this.statefulRedisConnection.sync().del(this.rKey)
        }
    }

    private fun rethrowAsLockException(e: Exception) {
        throw CannotAcquireLockException("Failed to lock mutex at " + this.rKey, e)
    }

    private fun watchDog(threshold: Long, leaseRenewalTime: Long, times: AtomicInteger) {
        val expire = this.statefulRedisConnection.sync().ttl(this.rKey) * 1000
        if (expire > 0 && isAcquiredInThisProcess()) {
            if (expire <= threshold) {
                times.incrementAndGet()
                this.statefulRedisConnection.sync().pexpire(this.rKey, expire + leaseRenewalTime)
            }
        } else {
            throw RuntimeException("exit watchDog.")
        }
    }
}
