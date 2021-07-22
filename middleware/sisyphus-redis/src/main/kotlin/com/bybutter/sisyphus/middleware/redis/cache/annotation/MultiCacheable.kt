package com.bybutter.sisyphus.middleware.redis.cache.annotation

import com.bybutter.sisyphus.middleware.redis.cache.CacheAdapter
import com.bybutter.sisyphus.middleware.redis.cache.CacheType
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS
)
@Inherited
annotation class MultiCacheable(
        val key: String = "",
        val ttl: Long = 600,
        val cacheType: CacheType = CacheType.NORMAL,
        val qualifier: String = "",
        val cacheAdapter: KClass<out CacheAdapter> = CacheAdapter::class,
        val batchParamName: String = "",
        val serializer: KClass<out RedisSerializer<Any>> = RedisSerializer.json()::class,
        val remCount: Int = 8
)
