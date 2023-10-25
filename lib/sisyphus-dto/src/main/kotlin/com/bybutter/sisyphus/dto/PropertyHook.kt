package com.bybutter.sisyphus.dto

import kotlin.reflect.KClass

/**
 * Hooks for dto properties, it can be attached to getter, setter or both of them.
 */
@Repeatable
@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS)
annotation class PropertyHook(
    val value: KClass<out PropertyHookHandler<*>> = PropertyHookHandler::class,
    /**
     * Parameters for [PropertyHookHandler]
     */
    vararg val params: String = [],
)
