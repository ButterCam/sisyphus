@file:JvmName("CacheManagerKt")

package com.bybutter.sisyphus.middleware.cache.dsl

import com.bybutter.sisyphus.middleware.cache.CacheProvider
import com.bybutter.sisyphus.security.md5
import com.bybutter.sisyphus.string.randomNumberString
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32
import kotlin.math.pow

open class CacheManager<T, V>(
    val keyGenerator: (T) -> String = { it as String },
    val valueGenerator: (V?) -> V? = { it }
) {
    private val dynamicCache: CacheManager<String, String> by lazy {
        CacheManager({ "$DYNAMIC_KEY_PREFIX:$it" })
    }

    suspend fun cache(cacheProvider: CacheProvider, keyParam: T, scenes: CacheScenes, action: suspend () -> V?): V? {
        return cache(cacheProvider, keyParam, scenes.duration, scenes.unit, action)
    }

    suspend fun cache(
        cacheProvider: CacheProvider,
        keyParam: T,
        ttl: Long = 600,
        action: suspend () -> V?
    ): V? {
        return cache(cacheProvider, keyParam, ttl, TimeUnit.SECONDS, action)
    }

    suspend fun cache(
        cacheProvider: CacheProvider,
        keyParam: T,
        ttl: Long = TimeUnit.MINUTES.toSeconds(30),
        timeUnit: TimeUnit,
        action: suspend () -> V?
    ): V? {
        return cacheable(cacheProvider, keyGenerator(keyParam), ttl, timeUnit, action)
    }

    suspend fun batchCache(
        cacheProvider: CacheProvider,
        keyParams: Collection<T>,
        cacheAdapter: (List<V>) -> Map<String, V>,
        scenes: CacheScenes,
        action: suspend (Collection<T>) -> List<V>?
    ): List<V>? {
        return batchCache(
            cacheProvider,
            keyParams,
            cacheAdapter,
            scenes.duration,
            scenes.unit,
            action
        )
    }

    suspend fun batchCache(
        cacheProvider: CacheProvider,
        keyParams: Collection<T>,
        cacheAdapter: (List<V>) -> Map<String, V>,
        ttl: Long,
        action: suspend (Collection<T>) -> List<V>?
    ): List<V>? {
        return batchCache(
            cacheProvider,
            keyParams,
            cacheAdapter,
            ttl,
            TimeUnit.SECONDS,
            action
        )
    }

    suspend fun batchCache(
        cacheProvider: CacheProvider,
        keyParams: Collection<T>,
        cacheAdapter: (List<V>) -> Map<String, V>,
        ttl: Long = TimeUnit.MINUTES.toSeconds(30),
        timeUnit: TimeUnit,
        action: suspend (Collection<T>) -> List<V>?
    ): List<V>? {
        return batchCacheable(
            cacheProvider,
            keyParams.map { BatchKey(it.toString(), keyGenerator(it)) },
            cacheAdapter,
            ttl,
            timeUnit,
            action
        )
    }

    suspend fun hashCache(
        cacheProvider: CacheProvider,
        keyParam: T,
        hashKey: String,
        scenes: CacheScenes,
        action: suspend () -> V?
    ): V? {
        return hashCache(cacheProvider, keyParam, hashKey, scenes.duration, scenes.unit, action)
    }

    suspend fun hashCache(
        cacheProvider: CacheProvider,
        keyParam: T,
        hashKey: String,
        ttl: Long = 600,
        action: suspend () -> V?
    ): V? {
        return hashCache(cacheProvider, keyParam, hashKey, ttl, TimeUnit.SECONDS, action)
    }

    suspend fun hashCache(
        cacheProvider: CacheProvider,
        keyParam: T,
        hashKey: String,
        ttl: Long,
        timeUnit: TimeUnit,
        action: suspend () -> V?
    ): V? {
        return cacheable(cacheProvider, "${keyGenerator(keyParam)}:${hashKey.toCRC32().rem(8)}", ttl, timeUnit, action)
    }

    suspend fun randomHashCache(
        cacheProvider: CacheProvider,
        keyParam: T,
        randomLength: Int,
        scenes: CacheScenes,
        action: suspend () -> V?
    ): V? {
        return randomHashCache(cacheProvider, keyParam, randomLength, scenes.duration, scenes.unit, action)
    }

    suspend fun randomHashCache(
        cacheProvider: CacheProvider,
        keyParam: T,
        randomLength: Int,
        duration: Long = 600,
        action: suspend () -> V?
    ): V? {
        return randomHashCache(cacheProvider, keyParam, randomLength, duration, TimeUnit.SECONDS, action)
    }

    suspend fun randomHashCache(
        cacheProvider: CacheProvider,
        keyParam: T,
        randomLength: Int,
        ttl: Long = TimeUnit.MINUTES.toSeconds(30),
        timeUnit: TimeUnit,
        action: suspend () -> V?
    ): V? {
        return cacheable(
            cacheProvider,
            "${keyGenerator(keyParam)}:${randomNumberString(randomLength)}",
            ttl,
            timeUnit,
            action
        )
    }

    suspend fun cacheWithCondition(
        cacheProvider: CacheProvider,
        keyParam: T,
        condition: String?,
        scenes: CacheScenes,
        action: suspend () -> V?
    ): V? {
        return cacheWithCondition(cacheProvider, keyParam, condition, scenes.duration, scenes.unit, action)
    }

    suspend fun cacheWithCondition(
        cacheProvider: CacheProvider,
        keyParam: T,
        condition: String?,
        ttl: Long = 600,
        action: suspend () -> V?
    ): V? {
        return cacheWithCondition(cacheProvider, keyParam, condition, ttl, action)
    }

    suspend fun cacheWithCondition(
        cacheProvider: CacheProvider,
        keyParam: T,
        condition: String?,
        ttl: Long,
        timeUnit: TimeUnit,
        action: suspend () -> V?
    ): V? {
        val key = keyGenerator(keyParam)
        val dynamicKey = dynamicCache.randomHashCache(cacheProvider, key, DYNAMIC_KEY_RANDOM_LENGTH, ttl) {
            System.currentTimeMillis().toString()
        }
        val conditionKey = condition?.md5()?.let {
            "$key:$dynamicKey:$it"
        } ?: "$key:$dynamicKey"
        return cacheable(cacheProvider, conditionKey, ttl, timeUnit, action)
    }

    suspend fun batchCacheCondition(
        cacheProvider: CacheProvider,
        keyParams: Collection<T>,
        cacheAdapter: (List<V>) -> Map<String, V>,
        condition: String?,
        scenes: CacheScenes,
        action: suspend (Collection<T>) -> List<V>?
    ): List<V>? {
        return batchCacheCondition(
            cacheProvider,
            keyParams,
            cacheAdapter,
            condition,
            scenes.duration,
            scenes.unit,
            action
        )
    }

    suspend fun batchCacheCondition(
        cacheProvider: CacheProvider,
        keyParams: Collection<T>,
        cacheAdapter: (List<V>) -> Map<String, V>,
        condition: String?,
        ttl: Long = 600,
        action: suspend (Collection<T>) -> List<V>?
    ): List<V>? {
        return batchCacheCondition(
            cacheProvider,
            keyParams,
            cacheAdapter,
            condition,
            ttl,
            TimeUnit.SECONDS,
            action
        )
    }

    suspend fun batchCacheCondition(
        cacheProvider: CacheProvider,
        keyParams: Collection<T>,
        cacheAdapter: (List<V>) -> Map<String, V>,
        condition: String?,
        ttl: Long,
        timeUnit: TimeUnit,
        action: suspend (Collection<T>) -> List<V>?
    ): List<V>? {
        val batchKeys = keyParams.map {
            val key = keyGenerator(it)
            val dynamicKey = dynamicCache.randomHashCache(cacheProvider, key, DYNAMIC_KEY_RANDOM_LENGTH, ttl) {
                System.currentTimeMillis().toString()
            }
            BatchKey(
                it.toString(),
                condition?.md5()?.let {
                    "$key:$dynamicKey:$it"
                } ?: "$key:$dynamicKey"
            )
        }
        return batchCacheable(cacheProvider, batchKeys, cacheAdapter, ttl, timeUnit, action)
    }

    suspend fun evict(cacheProvider: CacheProvider, keyParam: T) {
        evict(cacheProvider, keyGenerator(keyParam))
    }

    suspend fun evict(cacheProvider: CacheProvider, keyParam: T, action: suspend () -> V?): V? {
        return action().also {
            evict(cacheProvider, keyGenerator(keyParam))
        }
    }

    suspend fun evictRandomHash(cacheProvider: CacheProvider, keyParam: T, randomLength: Int) {
        if (randomLength > 2) return
        val key = keyGenerator(keyParam)
        for (i in 0..(10f.pow(randomLength) - 1).toInt()) {
            evict(cacheProvider, "$key:$i")
        }
    }

    suspend fun evictWithConditionKey(cacheProvider: CacheProvider, keyParam: T) {
        dynamicCache.evictRandomHash(cacheProvider, keyGenerator(keyParam), 1)
    }

    private suspend fun evict(cacheProvider: CacheProvider, key: String) {
        cacheProvider.remove(key)
    }

    suspend fun cacheable(
        cacheProvider: CacheProvider,
        key: String,
        ttl: Long,
        timeUnit: TimeUnit,
        action: suspend () -> V?
    ): V? {
        val cacheValue = cacheProvider.get(key)
        return valueGenerator(
            if (cacheValue == null) {
                action()?.also {
                    cacheProvider.add(key, ttl, timeUnit, it)
                }
            } else {
                cacheValue as V?
            }
        )
    }

    suspend fun batchCacheable(
        cacheProvider: CacheProvider,
        batchKeys: List<BatchKey>,
        cacheAdapter: (List<V>) -> Map<String, V>,
        ttl: Long = 600,
        timeUnit: TimeUnit,
        action: suspend (Collection<T>) -> List<V>?
    ): List<V>? {
        val notHitKeys = mutableListOf<T>()
        val returnValue = mutableListOf<V>()
        for (paramKey in batchKeys) {
            val value = cacheProvider.get(paramKey.key)
            if (value == null) {
                notHitKeys.add(paramKey.param as T)
            } else {
                (value as? V)?.let {
                    returnValue.add(it)
                }
            }
        }
        if (notHitKeys.isEmpty()) {
            return returnValue.mapNotNull { valueGenerator(it) }
        }
        val invokeValue = action(notHitKeys)?.takeIf { it.isNotEmpty() } ?: return returnValue
        val notHitValueMap = cacheAdapter(invokeValue)
        val paramKeyMap = batchKeys.associate { it.param to it.key }
        for (valueMap in notHitValueMap) {
            val cacheKey = paramKeyMap[valueMap.key] ?: continue
            cacheProvider.add(cacheKey, ttl, timeUnit, valueMap.value)
        }
        returnValue.addAll(invokeValue as Collection<V>)
        return returnValue.mapNotNull { valueGenerator(it) }
    }

    private fun String.toCRC32(): Long {
        val bytes = this.toByteArray()
        val checksum = CRC32() // java.util.zip.CRC32
        checksum.update(bytes, 0, bytes.size)
        return checksum.value
    }

    companion object {
        const val DYNAMIC_KEY_PREFIX = "dynamic"
        const val DYNAMIC_KEY_RANDOM_LENGTH = 1
    }
}
