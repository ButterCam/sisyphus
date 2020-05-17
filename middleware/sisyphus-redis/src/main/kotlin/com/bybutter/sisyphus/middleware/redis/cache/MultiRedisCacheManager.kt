package com.bybutter.sisyphus.middleware.redis.cache

import java.time.Duration
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import org.springframework.cache.Cache
import org.springframework.data.redis.cache.RedisCache
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter

class MultiRedisCacheManager(private val cacheWriter: RedisCacheWriter, private val defaultCacheConfig: RedisCacheConfiguration, private val initialCacheConfiguration: Map<String, RedisCacheConfiguration> = hashMapOf(), val allowInFlightCacheCreation: Boolean = true) :
    RedisCacheManager(cacheWriter, defaultCacheConfig, initialCacheConfiguration, allowInFlightCacheCreation) {
    private val cacheMap: ConcurrentMap<String, Cache> = ConcurrentHashMap(16)

    @Volatile
    private var cacheNames = emptySet<String>()

    private fun getMissingCacheTtl(name: String, ttl: Long): RedisCache {
        val config = defaultCacheConfig.entryTtl(Duration.ofSeconds(ttl))
        return super.createRedisCache(name, config)
    }

    fun getCacheWithTtl(name: String, ttl: Long): Cache? {
        var cache = cacheMap[name]
        if (cache != null) {
            return cache
        }
        val missingCache: Cache = getMissingCacheTtl(name, ttl)
        synchronized(cacheMap) {
            cache = cacheMap[name]
            if (cache == null) {
                cache = decorateCache(missingCache)
                cacheMap[name] = cache
                updateCacheNames(name)
            }
        }
        return cache
    }

    private fun updateCacheNames(name: String) {
        val cacheNames: MutableSet<String> = LinkedHashSet(this.cacheNames.size + 1)
        cacheNames.addAll(this.cacheNames)
        cacheNames.add(name)
        this.cacheNames = Collections.unmodifiableSet(cacheNames)
    }
}
