/*
 * Copyright reserved by Beijing Muke Technology Co., Ltd. 2018
 */

package com.bybutter.sisyphus.middleware.cache.local

import com.bybutter.sisyphus.middleware.cache.CacheProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MemoryCache : CacheProvider {
    private val cacheMap: MutableMap<Any, CacheValueWrapper<Any>> = ConcurrentHashMap()

    companion object {
        @Volatile
        var threshold = 20
    }

    override suspend fun add(key: Any, value: Any?) {
        dataValidation(cacheMap[key], key)?.get()?.let {
            it.value = value
            return
        }
        cacheMap[key] = CacheValueWrapper(value)
        clearExpireData()
    }

    override suspend fun add(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?) {
        if (ttl != 0L) {
            cacheMap[key] = CacheValueWrapper(value, timeUnit.toMillis(ttl))
        } else {
            this.remove(key)
        }
        clearExpireData()
    }

    override suspend fun addIfAbsent(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?): Any? {
        return cacheMap[key]?.let {
            return null
        } ?: cacheMap.put(key, CacheValueWrapper(value, timeUnit.toMillis(ttl)))?.getValue()
    }

    override suspend fun getOrPut(key: Any, value: Any?): Any? {
        val valueWrapper = dataValidation(cacheMap[key], key)?.get() ?: CacheValueWrapper(value).apply {
            cacheMap[key] = this
        }.get()
        clearExpireData()
        return valueWrapper?.value
    }

    override suspend fun getOrPut(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?): Any? {
        val valueWrapper = dataValidation(cacheMap[key], key)?.get() ?: CacheValueWrapper(
            value,
            timeUnit.toMillis(ttl)
        ).apply {
            if (ttl != 0L) {
                cacheMap[key] = this
            } else {
                cacheMap.remove(key)
            }
        }.get()
        clearExpireData()
        return valueWrapper?.value
    }

    override suspend fun get(key: Any): Any? {
        return dataValidation(cacheMap[key], key)?.getValue()
    }

    override suspend fun remove(key: Any) {
        cacheMap.remove(key)
    }

    override suspend fun remove(keys: Collection<Any>) {
        for (key in keys) {
            cacheMap.remove(key)
        }
    }

    override suspend fun expire(key: Any, ttl: Long, timeUnit: TimeUnit) {
        TODO("Not yet implemented")
    }

    override suspend fun incr(key: Any): Long? {
        TODO("Not yet implemented")
    }

    private suspend fun dataValidation(cache: CacheValueWrapper<Any>?, key: Any): CacheValueWrapper<Any>? {
        cache ?: return null
        return if (cache.expired) {
            this.remove(arrayListOf(key))
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

    private fun convertKey(key: Any): String? {
        return if (key is String) {
            key
        } else null
    }

    private class CacheValueWrapper<T> {
        private var value: ValueWrapper<T?>
        private val expireAt: Long?

        val expired: Boolean get() = this.expireAt != null && this.expireAt < System.currentTimeMillis()

        constructor(value: T?, ttl: Long) {
            this.value = ValueWrapper(value)
            this.expireAt = System.currentTimeMillis() + ttl
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
