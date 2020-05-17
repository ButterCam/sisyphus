package com.bybutter.sisyphus.dto

import kotlin.reflect.KClass

/**
 * Dto object validating annotation, [validator] will be called with [DtoModel.verify] and [DtoModel.isValid].
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class DtoValidation(val validator: KClass<out DtoValidator<*>>, val params: Array<String> = [])

@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class DtoValidations(val validations: Array<DtoValidation>)
