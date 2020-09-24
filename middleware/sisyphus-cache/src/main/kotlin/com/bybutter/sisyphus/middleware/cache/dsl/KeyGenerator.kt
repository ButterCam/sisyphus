package com.bybutter.sisyphus.middleware.cache.dsl

import com.bybutter.sisyphus.middleware.cache.CacheProvider
import com.bybutter.sisyphus.security.base64

object KeyGenerator {

    private const val DYNAMIC_KEY_PREFIX = "dynamicKey"

    fun getDynamicIndex(key: String): String {
        return "$DYNAMIC_KEY_PREFIX:$key"
    }

    suspend fun generateDynamicKey(
        cacheProvider: CacheProvider,
        key: String,
        condition: String?,
        ttl: Long
    ): String {
        if (condition == null) return key
        val keyWithIndex = getOrIncr(cacheProvider, getDynamicIndex(key), ttl)?.let {
            "$key:$it"
        } ?: key
        return condition.takeIf { it.isNotEmpty() }?.let { con ->
            "$keyWithIndex:${con.base64()}"
        } ?: keyWithIndex
    }

    suspend fun getOrIncr(cacheProvider: CacheProvider, key: String, ttl: Long): Long? {
        return (cacheProvider.get(key)?.toString()?.toLong() ?: cacheProvider.incr(key))?.also {
            cacheProvider.expire(
                key,
                ttl
            )
        }
    }

    fun generateGlobalKey(key: Any, rem: Int): Set<Any> {
        val globalKeys = mutableSetOf<Any>()
        for (i in 0 until rem) {
            (key as? String)?.let {
                globalKeys.add("$it:$i")
            } ?: globalKeys.add(key)
        }
        return globalKeys
    }

    suspend fun getBatchKeys(
        key: String,
        params: Collection<String>
    ): Set<String> {
        val name = """\{.*?}""".toRegex().find(key)?.value ?: return emptySet()
        return params.map {
            key.replace(name, it)
        }.toSet()
    }

    suspend fun generateBatchKeys(
        cacheProvider: CacheProvider,
        key: String,
        params: Collection<String>,
        condition: String?,
        ttl: Long
    ): Set<BatchKey>? {
        val name = """\{.*?}""".toRegex().find(key)?.value ?: return null
        return params.map {
            BatchKey(it, generateDynamicKey(cacheProvider, key.replace(name, it), condition, ttl))
        }.toSet()
    }
}

data class BatchKey(val param: String, val key: String)
