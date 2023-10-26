package com.bybutter.sisyphus.dto

import java.lang.reflect.Proxy
import java.lang.reflect.Type

/**
 * Meta info for dto object, all dto instance can be cast to [DtoMeta].
 * It provide the unsafe and basic access for dto objects.
 */
interface DtoMeta {
    /**
     * The dto object real type.
     */
    @get:MetaProperty
    @Suppress("ktlint:standard:property-naming")
    val `$type`: Type

    /**
     * The map which used for store all properties for dto.
     */
    @get:MetaProperty
    @Suppress("ktlint:standard:property-naming")
    var `$modelMap`: MutableMap<String, Any?>

    /**
     * If current dot object should contain type info in json or other data format.
     */
    @get:MetaProperty
    @Suppress("ktlint:standard:property-naming")
    var `$outputType`: Boolean

    /**
     * Get the properties value for dto object, no hooks, no default values.
     */
    operator fun <T> get(name: String): T?

    /**
     * Set the properties value for dto object, no hooks.
     */
    operator fun <T> set(
        name: String,
        value: T?,
    )
}

fun DtoModel.hasProperty(name: String): Boolean {
    return (this as DtoMeta).hasProperty(name)
}

fun DtoMeta.hasProperty(name: String): Boolean {
    return this.`$modelMap`.containsKey(name) &&
        (Proxy.getInvocationHandler(this) as ModelProxy).properties.containsKey(
            name,
        )
}
