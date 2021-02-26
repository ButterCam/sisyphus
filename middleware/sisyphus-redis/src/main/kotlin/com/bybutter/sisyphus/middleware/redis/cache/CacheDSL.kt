/*
 * Copyright reserved by Beijing Muke Technology Co., Ltd. 2018
 */

package com.bybutter.sisyphus.middleware.redis.cache

import java.util.concurrent.TimeUnit

suspend fun <K, V> cacheGet(provider: CacheProvider<K, V>, key: K): V? {
    return provider.get(key)
}

suspend fun <K, V> cache(key: K, scenes: CacheScenes, action: suspend () -> V?): V? {
    return cache(CacheProvider.current as CacheProvider<K, V>, key, scenes, action)
}

suspend fun <K, V> cache(provider: CacheProvider<K, V>, key: K, scenes: CacheScenes, action: suspend () -> V?): V? {
    return cache(provider, key, scenes.duration, scenes.unit, action)
}

suspend fun <K, V> cache(key: K, duration: Long, unit: TimeUnit, action: suspend () -> V?): V? {
    return cache(CacheProvider.current as CacheProvider<K, V>, key, duration, unit, action)
}

suspend fun <K, V> cache(provider: CacheProvider<K, V>, key: K, action: suspend () -> V?): V? {
    return provider.getOrSet(key, action)
}

suspend fun <K, V> cache(
    provider: CacheProvider<K, V>,
    key: K,
    duration: Long,
    unit: TimeUnit,
    action: suspend () -> V?
): V? {
    return provider.getOrSet(key, duration, unit, action)
}

suspend fun <K, V> extendableCache(key: K, scenes: CacheScenes, action: suspend () -> V?): V? {
    return extendableCache(CacheProvider.current as CacheProvider<K, V>, key, scenes, action)
}

suspend fun <K, V> extendableCache(
    provider: CacheProvider<K, V>,
    key: K,
    scenes: CacheScenes,
    action: suspend () -> V?
): V? {
    return extendableCache(provider, key, scenes.duration, scenes.unit, action)
}

suspend fun <K, V> extendableCache(key: K, duration: Long, unit: TimeUnit, action: suspend () -> V?): V? {
    return extendableCache(CacheProvider.current as CacheProvider<K, V>, key, duration, unit, action)
}

suspend fun <K, V> extendableCache(
    provider: CacheProvider<K, V>,
    key: K,
    duration: Long,
    unit: TimeUnit,
    action: suspend () -> V?
): V? {
    return provider.extendableGetOrSet(key, duration, unit, action)
}

private data class CacheContext<K, V, T>(val target: T, val key: K, var value: V? = null)

suspend fun <K, V : Any, T> batchCache(
    provider: CacheProvider<K, V>,
    targets: Collection<T>,
    keyMapper: (T) -> K,
    scenes: CacheScenes,
    action: (Collection<T>) -> Collection<Pair<K, V>>
): List<V> {
    val contextMap = targets.map { CacheContext<K, V, T>(it, getCacheKeyForItem(keyMapper, it)) }.associateBy { it.key }

    fillAlreadyCached(contextMap, provider)

    val notCached = getNotCached(contextMap)
    if (notCached.isNotEmpty()) {
        action(notCached).forEach {
            fillNotCached(contextMap, it)
            cacheNotCached(provider, it, scenes)
        }
    }

    return contextMap.mapNotNull { it.value.value }
}

private suspend fun <K, V : Any> cacheNotCached(provider: CacheProvider<K, V>, it: Pair<K, V>, scenes: CacheScenes) {
    provider.set(it.first, it.second, scenes.duration, scenes.unit)
}

private suspend fun <K, T, V : Any> fillNotCached(contextMap: Map<K, CacheContext<K, V, T>>, it: Pair<K, V>) {
    contextMap[it.first]?.value = it.second
}

private fun <K, T, V : Any> getNotCached(contextMap: Map<K, CacheContext<K, V, T>>) =
    contextMap.values.filter { it.value == null }.map { it.target }

private suspend fun <K, T, V : Any> fillAlreadyCached(
    contextMap: Map<K, CacheContext<K, V, T>>,
    provider: CacheProvider<K, V>
) {
    contextMap.values.forEach {
        it.value = provider.get(it.key)
    }
}

private fun <K, T> getCacheKeyForItem(keyMapper: (T) -> K, it: T) = keyMapper(it)
