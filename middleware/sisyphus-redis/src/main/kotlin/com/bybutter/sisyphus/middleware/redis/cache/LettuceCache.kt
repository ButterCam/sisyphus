package com.bybutter.sisyphus.middleware.redis.cache

import io.lettuce.core.RedisClient
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.future.await

class LettuceCache(private val client: RedisClient) : CacheProvider<String, String> {
    override suspend fun get(key: String): String? {
        return client.connect().async().get(key).await()
    }

    override suspend fun set(key: String, value: String?) {
        client.connect().async().set(key, value)
    }

    override suspend fun set(key: String, value: String?, duration: Long, unit: TimeUnit) {
        val commands = client.connect().async()
        if (duration != 0L) {
            commands.psetex(key, unit.toMillis(duration), value)
        } else {
            commands.del(key)
        }
    }

    override suspend fun getOrSet(key: String, action: suspend () -> String?): String? {
        val commands = client.connect().async()
        return commands.get(key).await() ?: action()?.apply {
            commands.set(key, this)
        }
    }

    override suspend fun getOrSet(key: String, duration: Long, unit: TimeUnit, action: suspend () -> String?): String? {
        val commands = client.connect().async()
        return commands.get(key).await() ?: action()?.apply {
            if (duration != 0L) {
                commands.psetex(key, unit.toMillis(duration), this)
            } else {
                commands.del(key)
            }
        }
    }

    override suspend fun increment(key: String, delta: Long): Long {
        return client.connect().async().incrby(key, delta).await()
    }

    override suspend fun extendableGetOrSet(
        key: String,
        duration: Long,
        unit: TimeUnit,
        action: suspend () -> String?
    ): String? {
        val commands = client.connect().async()
        return if (duration != 0L) {
            commands.expire(key, unit.toSeconds(duration))
            getOrSet(key, duration, unit, action)
        } else {
            commands.del(key)
            null
        }
    }

    override suspend fun remove(key: String) {
        client.connect().async().del(key)
    }

    override suspend fun contains(key: String): Boolean {
        val result = client.connect().async().get(key).await()
        return result != null
    }

    override suspend fun expire(key: String, duration: Long, unit: TimeUnit) {
        val commands = client.connect().async()
        if (duration != 0L) {
            commands.expire(key, unit.toSeconds(duration))
        } else {
            commands.del(key)
        }
    }

    override suspend fun clear() {
        throw UnsupportedOperationException("Can't clear redis cache")
    }
}
