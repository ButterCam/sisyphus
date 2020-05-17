/*
 * Copyright reserved by Beijing Muke Technology Co., Ltd. 2018
 */

package com.bybutter.sisyphus.middleware.redis.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MemoryCache<in K, V> : CacheProvider<K, V> {
    private val cacheMap: MutableMap<K, CacheValueWrapper<V>> = ConcurrentHashMap()

    companion object {
        @Volatile
        var threshold = 20
    }

    override suspend fun get(key: K): V? {
        return dataValidation(cacheMap[key], key)?.getValue()
    }

    override suspend fun set(key: K, value: V?) {
        dataValidation(cacheMap[key], key)?.get()?.let {
            it.value = value
            return
        }
        cacheMap[key] = CacheValueWrapper(value)
        clearExpireData()
    }

    override suspend fun set(key: K, value: V?, duration: Long, unit: TimeUnit) {
        if (duration != 0L) {
            cacheMap[key] = CacheValueWrapper(value, duration, unit)
        } else {
            this.remove(key)
        }
        clearExpireData()
    }

    override suspend fun getOrSet(key: K, action: suspend () -> V?): V? {
        val value = action()
        val valueWrapper = dataValidation(cacheMap[key], key)?.get() ?: {
            CacheValueWrapper(value).apply {
                cacheMap[key] = this
            }.get()
        }()
        clearExpireData()
        return valueWrapper?.value
    }

    override suspend fun getOrSet(key: K, duration: Long, unit: TimeUnit, action: suspend () -> V?): V? {
        val value = action()
        val valueWrapper = dataValidation(cacheMap[key], key)?.get() ?: {
            CacheValueWrapper(value, duration, unit).apply {
                if (duration != 0L) {
                    cacheMap[key] = this
                } else {
                    cacheMap.remove(key)
                }
            }.get()
        }()
        clearExpireData()
        return valueWrapper?.value
    }

    override suspend fun remove(key: K) {
        cacheMap.remove(key)
    }

    override suspend fun contains(key: K): Boolean {
        return cacheMap[key]?.get() != null
    }

    override suspend fun clear() {
        cacheMap.clear()
        threshold = 20
    }

    override suspend fun increment(key: K, delta: Long): Long {
        throw UnsupportedOperationException("MemoryCache don't support increment")
    }

    override suspend fun expire(key: K, duration: Long, unit: TimeUnit) {
        throw UnsupportedOperationException("Expire don't support increment")
    }

    override suspend fun extendableGetOrSet(key: K, duration: Long, unit: TimeUnit, action: suspend () -> V?): V? {
        TODO("not implemented")
    }

    private suspend fun dataValidation(cache: CacheValueWrapper<V>?, key: K): CacheValueWrapper<V>? {
        cache ?: return null
        return if (cache.expired) {
            this.remove(key)
            null
        } else {
            cache
        }
    }

    private fun clearExpireData() {
        // 判断是否达到阈值
        if (threshold > cacheMap.size) {
            return
        }

        kotlin.run {
            // 清理所有过期的key
            for ((key, value) in cacheMap) {
                if (value.expired) cacheMap.remove(key)
            }
            // 清理完成后验证是否需要扩容
            if (!expansion()) shrinkage()
        }
    }

    // 阈值扩容
    @Synchronized
    private fun expansion(): Boolean {
        return if (threshold <= cacheMap.size) {
            threshold *= 2
            true
        } else {
            false
        }
    }

    // 阈值缩容
    @Synchronized
    private fun shrinkage() {
        // 如果 阈值大于map size的两倍 则进行缩容
        if (threshold > (cacheMap.size * 2)) {
            // 缩容到当前size的1.5倍
            val size = cacheMap.size + (cacheMap.size / 2)
            threshold = if (size < 20) 20 else size
        }
    }

    private class CacheValueWrapper<T> {
        private var value: ValueWrapper<T?>
        private val expireAt: Long?

        val expired: Boolean get() = this.expireAt != null && this.expireAt < System.currentTimeMillis()

        constructor(value: T?, duration: Long, unit: TimeUnit) {
            this.value = ValueWrapper(value)
            this.expireAt = System.currentTimeMillis() + unit.toMillis(duration)
        }

        constructor(value: T?) {
            this.value = ValueWrapper(value)
            this.expireAt = null
        }

        fun setValue(value: T?) {
            this.value.value = value
        }

        fun getValue(): T? {
            return this.value.value
        }

        fun get(): ValueWrapper<T?>? {
            return this.value
        }
    }

    private class ValueWrapper<T>(var value: T?)
}
