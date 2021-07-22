package com.bybutter.sisyphus.middleware.cache

import java.util.concurrent.TimeUnit

interface CacheProvider {

    suspend fun add(key: Any, value: Any?)

    suspend fun add(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?)

    suspend fun addIfAbsent(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?): Any?

    suspend fun getOrPut(key: Any, value: Any?): Any?

    suspend fun getOrPut(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?): Any?

    suspend fun get(key: Any): Any?

    suspend fun remove(key: Any)

    suspend fun remove(keys: Collection<Any>)

    suspend fun expire(key: Any, ttl: Long, timeUnit: TimeUnit)

    suspend fun incr(key: Any): Long?
}
