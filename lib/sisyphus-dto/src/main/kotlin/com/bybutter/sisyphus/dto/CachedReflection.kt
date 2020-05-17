package com.bybutter.sisyphus.dto

import com.bybutter.sisyphus.reflect.instance
import com.bybutter.sisyphus.reflect.uncheckedCast
import java.lang.reflect.Method
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

interface CachedReflection {
    val properties: Map<String, KProperty1<out Any, Any?>>
    val getters: Map<Method, KProperty1<out Any, Any?>>
    val setters: Map<Method, KMutableProperty1<out Any, *>>
    val defaultValue: Map<String, DefaultValueAssigning?>
    val dtoValidators: List<DtoValidating>
    val propertyValidators: Map<String, List<PropertyValidating>>
    val getterHooks: Map<String, List<PropertyHooking>>
    val setterHooks: Map<String, List<PropertyHooking>>
    val notNullProperties: Set<KProperty1<out Any, Any?>>
}

data class DtoValidating(
    val raw: DtoValidation,
    val instance: DtoValidator<DtoModel> = raw.validator.instance().uncheckedCast()
)

data class PropertyValidating(
    val raw: PropertyValidation,
    val instance: PropertyValidator<in Any?> = raw.validator.instance().uncheckedCast()
)

data class PropertyHooking(
    val raw: PropertyHook,
    val instance: PropertyHookHandler<Any?> = raw.value.instance().uncheckedCast()
)

data class DefaultValueAssigning(
    val raw: DefaultValue,
    val instance: DefaultValueProvider<Any?> = raw.valueProvider.instance().uncheckedCast()
)

internal fun DtoValidation.resolve(): DtoValidating {
    return DtoValidating(this)
}

internal fun PropertyValidation.resolve(): PropertyValidating {
    return PropertyValidating(this)
}

internal fun PropertyHook.resolve(): PropertyHooking {
    return PropertyHooking(this)
}

internal fun DefaultValue.resolve(): DefaultValueAssigning {
    return if (this.valueProvider == DefaultValueProvider::class) {
        DefaultValueAssigning(this, DefaultValueProvider.Default)
    } else {
        DefaultValueAssigning(this)
    }
}
