package com.bybutter.sisyphus.dto

import kotlin.reflect.KClass

/**
 * Dto property validating annotation, [validator] will be called with property setter, [DtoModel.verify] and [DtoModel.isValid].
 */
@Target(AnnotationTarget.PROPERTY_SETTER)
annotation class PropertyValidation(val validator: KClass<out PropertyValidator<*>>, val params: Array<String> = [])

@Target(AnnotationTarget.PROPERTY_SETTER)
annotation class PropertyValidations(val validations: Array<PropertyValidation>)
