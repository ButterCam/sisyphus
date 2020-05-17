package com.bybutter.sisyphus.dto

/**
 * Mark a dto property is meta property, it should not be serializing.
 */
@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class MetaProperty
