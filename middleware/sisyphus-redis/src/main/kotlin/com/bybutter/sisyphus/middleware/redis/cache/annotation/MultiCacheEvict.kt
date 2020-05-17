package com.bybutter.sisyphus.middleware.redis.cache.annotation

import com.bybutter.sisyphus.middleware.redis.cache.CacheAdapter
import com.bybutter.sisyphus.middleware.redis.cache.CacheType
import java.lang.annotation.Documented
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
annotation class MultiCacheEvict(
    val cacheName: String = "",
    val key: String = "",
    val ttl: Long = 600,
    val cacheType: CacheType = CacheType.NORMAL,
    val cacheAdapter: KClass<out CacheAdapter> = CacheAdapter::class,
    val remCount: Int = 8
)
