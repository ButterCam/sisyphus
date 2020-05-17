package com.bybutter.sisyphus.dto

import kotlin.reflect.KClass

/**
 * Default value for dto properties, implements custom [DefaultValueProvider]
 * to create custom type value.
 *
 * If [valueProvider] not be provided, it can convert basic type only. Any other
 * type will be case a exception.
 */
@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class DefaultValue(
    /**
     * The string value of default value, you should implements [DefaultValueProvider]
     * to support non-basic type.
     */
    val value: String = "",
    val assign: Boolean = false,
    /**
     * The provider which can convert string value to real type value.
     *
     * If not provided, the default provider can convert string value to basic type.
     * Here is all supported type and convert function used in default implementation:
     * - Long: [String.toLong]
     * - Int: [String.toInt]
     * - Short: [String.toShort]
     * - Byte: [String.toByte]
     * - Char: [String.first]
     * - Boolean: [String.toBoolean]
     * - Float: [String.toFloat]
     * - Double: [String.toDouble]
     * - String: [String.toString]
     */
    val valueProvider: KClass<out DefaultValueProvider<*>> = DefaultValueProvider::class
)
