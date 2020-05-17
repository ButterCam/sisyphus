package com.bybutter.sisyphus.dto

/**
 * Mark a dto property is nullable, should be skip null check in dto validation.
 */
@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class NullableProperty
