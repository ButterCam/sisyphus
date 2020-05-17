/*
 * Copyright reserved by Beijing Muke Technology Co., Ltd. 2018
 */

package com.bybutter.sisyphus.middleware.redis.cache

import java.util.concurrent.TimeUnit
import kotlin.concurrent.getOrSet

interface CacheProvider<in K, V> {
    suspend fun get(key: K): V?

    suspend fun set(key: K, value: V?)

    suspend fun set(key: K, value: V?, duration: Long, unit: TimeUnit)

    suspend fun getOrSet(key: K, action: suspend () -> V?): V?

    suspend fun getOrSet(key: K, duration: Long, unit: TimeUnit, action: suspend () -> V?): V?

    suspend fun increment(key: K, delta: Long): Long

    suspend fun extendableGetOrSet(key: K, duration: Long, unit: TimeUnit, action: suspend () -> V?): V?

    suspend fun remove(key: K)

    suspend fun contains(key: K): Boolean

    suspend fun expire(key: K, duration: Long, unit: TimeUnit)

    suspend fun clear()

    companion object {
        private val threadCaching: ThreadLocal<CacheProvider<Any, Any>> = ThreadLocal()

        var current: CacheProvider<Any, Any> = MemoryCache()

        var threadCurrent: CacheProvider<Any, Any> = threadCaching.getOrSet {
            MemoryCache()
        }
    }
}
