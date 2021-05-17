package com.bybutter.sisyphus.middleware.redis.util

import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import io.lettuce.core.api.StatefulRedisConnection
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirstOrNull
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DistributedLock(private val redis: StatefulRedisConnection<String, String>) {
    private val timer = HashedWheelTimer()
    private val renewExpirationMap = ConcurrentHashMap<String, LockInfo>()

    suspend fun <T> lock(key: String, leaseTime: Long, timeUnit: TimeUnit, action: suspend () -> T?): T? {
        return if (lock(key, leaseTime, timeUnit)) {
            try {
                action()
            } finally {
                unlock(key)
            }
        } else {
            throw Exception("加锁失败~")
        }
    }

    suspend fun <T> tryLock(
        key: String,
        waitTime: Long,
        leaseTime: Long,
        timeUnit: TimeUnit,
        action: suspend () -> T?
    ): T? {
        return if (tryLock(key, waitTime, leaseTime, timeUnit)) {
            try {
                action()
            } finally {
                unlock(key)
            }
        } else {
            throw Exception("加锁失败~")
        }
    }

    suspend fun tryLock(key: String, waitTime: Long, leaseTime: Long, timeUnit: TimeUnit): Boolean {
        val endTime = System.currentTimeMillis() + timeUnit.toMillis(waitTime)
        while (System.currentTimeMillis() < endTime) {
            if (lock(key, leaseTime, timeUnit))
                return true
            delay(100)
        }
        return false
    }

    suspend fun lock(key: String, leaseTime: Long, timeUnit: TimeUnit): Boolean {
        val value = UUID.randomUUID().toString()
        return (
            redis.reactive().set(key, value, SetArgs().nx().ex(timeUnit.toSeconds(leaseTime)))
                .awaitFirstOrNull() != null
            ).also {
            if (it) {
                scheduleRenewExpiration(key, value, leaseTime, timeUnit)
            }
        }
    }

    private fun scheduleRenewExpiration(key: String, value: String, leaseTime: Long, timeUnit: TimeUnit) {
        renewExpirationMap.putIfAbsent(key, LockInfo(key, value))?.let {
            return
        } ?: renewExpiration(key, leaseTime, timeUnit)
    }

    private fun renewExpiration(key: String, leaseTime: Long, timeUnit: TimeUnit) {
        val lockInfo = renewExpirationMap[key] ?: return
        lockInfo.timeout = timer.newTimeout(
            {
                redis.sync().eval<Long>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('expire', KEYS[1],ARGV[2]) else return 0 end",
                    ScriptOutputType.INTEGER,
                    arrayOf(key),
                    lockInfo.value,
                    leaseTime.toString()
                ).takeIf { it == 1L }?.let {
                    renewExpiration(key, leaseTime, timeUnit)
                }
            },
            leaseTime * 2 / 3, timeUnit
        )
    }

    suspend fun unlock(key: String) {
        val lockInfo = renewExpirationMap[key] ?: return
        redis.reactive().eval<Long>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            ScriptOutputType.INTEGER,
            arrayOf(key),
            lockInfo.value
        ).awaitFirstOrNull().takeIf { it == 1L }?.let {
            cancelRenewExpiration(key)
        }
    }

    private fun cancelRenewExpiration(key: String) {
        val lockInfo = renewExpirationMap[key] ?: return
        lockInfo.timeout?.cancel()
        renewExpirationMap.remove(key)
    }
}

data class LockInfo(val key: String, val value: String, var timeout: Timeout? = null)
