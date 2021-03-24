package com.bybutter.sisyphus.middleware.redis.cache.interceptor

import com.bybutter.sisyphus.middleware.redis.cache.CacheType
import com.bybutter.sisyphus.middleware.redis.cache.MultiRedisCacheManager
import com.bybutter.sisyphus.middleware.redis.cache.annotation.MultiCacheable
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.common.KeyGenerator.generateBatchKey
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.common.KeyGenerator.generateGlobalKey
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.common.KeyGenerator.generateNormalKey
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.cache.Cache
import java.io.Serializable
import java.lang.reflect.Method

class MultiCacheableInterceptor(
    private val redisCacheManager: MultiRedisCacheManager,
    private val evaluationContextInterceptorList: List<EvaluationContextInterceptor>
) : MethodInterceptor, Serializable {

    override fun invoke(invocation: MethodInvocation): Any? {

        val method = invocation.method
        val multiCacheable = method.getAnnotation(MultiCacheable::class.java)
        val cacheName = multiCacheable.cacheName
        val ttl = multiCacheable.ttl
        val multiRedisCache = redisCacheManager.getCacheWithTtl(cacheName, ttl)
        return when (multiCacheable.cacheType) {
            CacheType.NORMAL -> normalCache(invocation, method, multiCacheable, multiRedisCache)
            CacheType.BATCH -> batchCache(invocation, method, multiCacheable, multiRedisCache)
            CacheType.GLOBAL -> globalCache(invocation, method, multiCacheable, multiRedisCache)
        }
    }

    private fun normalCache(
        invocation: MethodInvocation,
        method: Method,
        multiCacheable: MultiCacheable,
        multiRedisCache: Cache?
    ): Any? {
        val key =
            generateNormalKey(invocation, multiCacheable.key, method, evaluationContextInterceptorList)?.toString()
                ?: return invocation.proceed()
        return cache(key, invocation, multiRedisCache)
    }

    private fun globalCache(
        invocation: MethodInvocation,
        method: Method,
        multiCacheable: MultiCacheable,
        multiRedisCache: Cache?
    ): Any? {
        val key = generateGlobalKey(
            invocation,
            multiCacheable.key,
            method,
            multiCacheable.remCount,
            evaluationContextInterceptorList
        )
            ?: return invocation.proceed()
        return cache(key, invocation, multiRedisCache)
    }

    private fun cache(key: Any, invocation: MethodInvocation, multiRedisCache: Cache?): Any? {
        val value = multiRedisCache?.get(key)?.get()
        return if (value == null) {
            val invokeValue = invocation.proceed()
            multiRedisCache?.put(key, invokeValue)
            invokeValue
        } else {
            value
        }
    }

    private fun batchCache(
        invocation: MethodInvocation,
        method: Method,
        multiCacheable: MultiCacheable,
        multiRedisCache: Cache?
    ): Any? {
        // 如果key获取失败（在batch中有使用key赋值没有使用spEL表达式，多个）
        val keys = generateBatchKey(invocation, multiCacheable.key, method, evaluationContextInterceptorList)
            ?: return invocation.proceed()
        val notHitKeys = mutableListOf<Any>()
        val returnValue = mutableListOf<Any>()
        for (key in keys) {
            val value = multiRedisCache?.get(key.toString())?.get()
            if (value == null) {
                notHitKeys.add(key.toString())
            } else {
                returnValue.add(value)
            }
        }
        if (notHitKeys.isEmpty()) {
            return returnValue
        }
        invocation.arguments[0] = notHitKeys
        val notHitValue = invocation.proceed()
        returnValue.addAll(notHitValue as Collection<Any>)
        val cacheAdapter = multiCacheable.cacheAdapter.java.newInstance()
        val notHitValueMap = cacheAdapter.cover(notHitValue)
        notHitValueMap.forEach {
            multiRedisCache?.put(it.key, it.value)
        }
        return returnValue
    }
}
