package com.bybutter.sisyphus.middleware.redis

import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import java.util.UUID
import java.util.concurrent.TimeUnit

suspend fun <T> StatefulRedisConnection<String, String>.lock(action: suspend () -> T?): T? {
    return lock(action.javaClass.toString(), action)
}

suspend fun <T> StatefulRedisConnection<String, String>.lock(key: String, action: suspend () -> T?): T? {
    return lock(key, 60, 30, TimeUnit.SECONDS, action)
}

suspend fun <T> StatefulRedisConnection<String, String>.lock(
    waitTime: Long,
    leaseTime: Long,
    timeUnit: TimeUnit,
    action: suspend () -> T?
): T? {
    return lock(action.javaClass.toString(), waitTime, leaseTime, timeUnit, action)
}

suspend fun <T> StatefulRedisConnection<String, String>.lock(
    key: String,
    waitTime: Long,
    leaseTime: Long,
    timeUnit: TimeUnit,
    action: suspend () -> T?
): T? {
    val distributedLock = DistributedLock(key, this)
    return if (distributedLock.lock(waitTime, leaseTime, timeUnit)) {
        try {
            action()
        } finally {
            distributedLock.unlock()
        }
    } else {
        throw Exception("加锁失败~")
    }
}

suspend fun <T> StatefulRedisConnection<String, String>.tryLock(action: suspend () -> T?): T? {
    return tryLock(action.javaClass.toString(), action)
}

suspend fun <T> StatefulRedisConnection<String, String>.tryLock(key: String, action: suspend () -> T?): T? {
    return tryLock(key, 30, TimeUnit.SECONDS, action)
}

suspend fun <T> StatefulRedisConnection<String, String>.tryLock(
    leaseTime: Long,
    timeUnit: TimeUnit,
    action: suspend () -> T?
): T? {
    return tryLock(action.javaClass.toString(), leaseTime, timeUnit, action)
}

suspend fun <T> StatefulRedisConnection<String, String>.tryLock(
    key: String,
    leaseTime: Long,
    timeUnit: TimeUnit,
    action: suspend () -> T?
): T? {
    val distributedLock = DistributedLock(key, this)
    return if (distributedLock.lock(leaseTime, timeUnit)) {
        try {
            action()
        } finally {
            distributedLock.unlock()
        }
    } else {
        throw Exception("加锁失败~")
    }
}

private class DistributedLock(private var key: String, private val redis: StatefulRedisConnection<String, String>) {
    private var released = false
    private var job: Job? = null
    private var value: String? = null

    suspend fun lock(waitTime: Long, leaseTime: Long, timeUnit: TimeUnit): Boolean {
        val endTime = System.currentTimeMillis() + timeUnit.toMillis(waitTime)
        while (System.currentTimeMillis() < endTime) {
            if (lock(leaseTime, timeUnit))
                return true
            delay(100)
        }
        return false
    }

    suspend fun lock(leaseTime: Long, timeUnit: TimeUnit): Boolean {
        value = UUID.randomUUID().toString()
        return (
            redis.reactive().set(key, value, SetArgs().nx().ex(timeUnit.toSeconds(leaseTime)))
                .awaitFirstOrNull() != null
            ).also {
            if (it) {
                renewExpiration(leaseTime, timeUnit)
            }
        }
    }

    private fun renewExpiration(leaseTime: Long, timeUnit: TimeUnit) {
        job = GlobalScope.launch(Dispatchers.IO) {
            delay(timeUnit.toMillis(leaseTime * 2 / 3))
            while (!released) {
                redis.sync().eval<Long>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('expire', KEYS[1],ARGV[2]) else return 0 end",
                    ScriptOutputType.INTEGER,
                    arrayOf(key),
                    value,
                    leaseTime.toString()
                ).takeIf { it == 1L } ?: kotlin.run {
                    released = true
                }
                delay(timeUnit.toMillis(leaseTime * 2 / 3))
            }
        }
    }

    suspend fun unlock() {
        redis.reactive().eval<Long>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            ScriptOutputType.INTEGER,
            arrayOf(key),
            value
        ).awaitFirstOrNull().takeIf { it == 1L }?.let {
            cancelRenewExpiration()
        }
    }

    private fun cancelRenewExpiration() {
        released = true
        job?.cancel()
    }
}
