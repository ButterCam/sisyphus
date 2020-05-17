package com.bybutter.sisyphus.middleware.redis.cache.interceptor

import com.bybutter.sisyphus.middleware.redis.cache.CacheType
import com.bybutter.sisyphus.middleware.redis.cache.MultiRedisCacheManager
import com.bybutter.sisyphus.middleware.redis.cache.annotation.MultiCacheEvict
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.common.KeyGenerator.generateBatchKey
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.common.KeyGenerator.generateNormalKey
import java.io.Serializable
import java.lang.reflect.Method
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory
import org.springframework.cache.Cache

class MultiCacheEvictInterceptor(private val redisCacheManager: MultiRedisCacheManager, private val evaluationContextInterceptorList: List<EvaluationContextInterceptor>) : MethodInterceptor, Serializable {

    val log = LoggerFactory.getLogger(this.javaClass)

    override fun invoke(invocation: MethodInvocation): Any? {
        val method = invocation.method
        val multiCacheEvict = method.getAnnotation(MultiCacheEvict::class.java)
        val cacheName = multiCacheEvict.cacheName
        val ttl = multiCacheEvict.ttl
        val multiRedisCache = redisCacheManager.getCacheWithTtl(cacheName, ttl)
        return when (multiCacheEvict.cacheType) {
            CacheType.NORMAL -> normalCacheEvict(invocation, method, multiCacheEvict, multiRedisCache)
            CacheType.GLOBAL -> globalCacheEvict(invocation, method, multiCacheEvict, multiRedisCache)
            CacheType.BATCH -> batchCacheEvict(invocation, method, multiCacheEvict, multiRedisCache)
        }
    }

    private fun normalCacheEvict(invocation: MethodInvocation, method: Method, multiCacheEvict: MultiCacheEvict, multiRedisCache: Cache?): Any? {
        val key = generateNormalKey(invocation, multiCacheEvict.key, method, evaluationContextInterceptorList)?.toString() ?: return invocation.proceed()
        return evict(key, invocation, multiRedisCache)
    }

    private fun evict(key: String, invocation: MethodInvocation, multiRedisCache: Cache?): Any? {
        multiRedisCache?.evictIfPresent(key)
        return invocation.proceed()
    }

    private fun globalCacheEvict(invocation: MethodInvocation, method: Method, multiCacheEvict: MultiCacheEvict, multiRedisCache: Cache?): Any? {
        for (remCount in 0 until multiCacheEvict.remCount) {
            multiRedisCache?.evictIfPresent(remCount.toString())
        }
        return invocation.proceed()
    }

    private fun batchCacheEvict(invocation: MethodInvocation, method: Method, multiCacheEvict: MultiCacheEvict, multiRedisCache: Cache?): Any? {
        val keys = generateBatchKey(invocation, multiCacheEvict.key, method, evaluationContextInterceptorList) ?: return invocation.proceed()
        for (key in keys) {
            multiRedisCache?.evictIfPresent(key)
        }
        return invocation.proceed()
    }
}
