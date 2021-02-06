package com.bybutter.sisyphus.middleware.redis.cache.interceptor

import com.bybutter.sisyphus.middleware.redis.cache.CacheType
import com.bybutter.sisyphus.middleware.redis.cache.MultiRedisCacheManager
import com.bybutter.sisyphus.middleware.redis.cache.annotation.MultiCachePut
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.common.KeyGenerator.generateGlobalKey
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.common.KeyGenerator.generateNormalKey
import com.bybutter.sisyphus.reflect.instance
import java.io.Serializable
import java.lang.reflect.Method
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.cache.Cache

class MultiCachePutInterceptor(private val redisCacheManager: MultiRedisCacheManager, private val evaluationContextInterceptorList: List<EvaluationContextInterceptor>) : MethodInterceptor, Serializable {

    override fun invoke(invocation: MethodInvocation): Any? {
        val method = invocation.method
        val multiCachePut = method.getAnnotation(MultiCachePut::class.java)
        val cacheName = multiCachePut.cacheName
        val ttl = multiCachePut.ttl
        val multiRedisCache = redisCacheManager.getCacheWithTtl(cacheName, ttl)
        return when (multiCachePut.cacheType) {
            CacheType.NORMAL -> normalCachePut(invocation, method, multiCachePut, multiRedisCache)
            CacheType.BATCH -> batchCachePut(invocation, multiCachePut, multiRedisCache)
            CacheType.GLOBAL -> globalCachePut(invocation, method, multiCachePut, multiRedisCache)
        }
    }

    private fun normalCachePut(invocation: MethodInvocation, method: Method, multiCachePut: MultiCachePut, multiRedisCache: Cache?): Any? {
        val key = generateNormalKey(invocation, multiCachePut.key, method, evaluationContextInterceptorList)?.toString() ?: return invocation.proceed()
        return cache(key, invocation, multiRedisCache)
    }

    private fun globalCachePut(invocation: MethodInvocation, method: Method, multiCachePut: MultiCachePut, multiRedisCache: Cache?): Any? {
        val key = generateGlobalKey(invocation, multiCachePut.key, method, multiCachePut.remCount, evaluationContextInterceptorList)
                ?: return invocation.proceed()
        return cache(key, invocation, multiRedisCache)
    }

    private fun cache(key: Any, invocation: MethodInvocation, multiRedisCache: Cache?): Any? {
        val invokeValue = invocation.proceed()
        multiRedisCache?.put(key, invokeValue)
        return invokeValue
    }

    private fun batchCachePut(invocation: MethodInvocation, multiCachePut: MultiCachePut, multiRedisCache: Cache?): Any? {
        // 如果key获取失败（在batch中有使用key赋值没有使用spEL表达式，多个）
        val invokeValue = invocation.proceed()
        val cacheAdapter = multiCachePut.cacheAdapter.instance()
        val notHitValueMap = cacheAdapter.cover(invokeValue)
        notHitValueMap.forEach {
            multiRedisCache?.put(it.key, it.value)
        }
        return invokeValue
    }
}
