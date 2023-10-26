package com.bybutter.sisyphus.dto

import com.bybutter.sisyphus.reflect.SimpleType
import com.bybutter.sisyphus.reflect.allProperties
import java.lang.reflect.Method
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter

/**
 * Cache all needed reflection of dto objects.
 */
object ReflectionCache {
    private val cache = mutableMapOf<SimpleType, CachedReflection>()

    private class CachedReflectionImpl(type: SimpleType) : CachedReflection {
        override val defaultValue: Map<String, DefaultValueAssigning?>
        override val dtoValidators: List<DtoValidating>
        override val getterHooks: Map<String, List<PropertyHooking>>
        override val setterHooks: Map<String, List<PropertyHooking>>
        override val propertyValidators: Map<String, List<PropertyValidating>>
        override val getters: Map<Method, KProperty1<out Any, Any?>>
        override val setters: Map<Method, KMutableProperty1<out Any, *>>
        override val notNullProperties: Set<KProperty1<out Any, Any?>>
        override val properties: Map<String, KProperty1<out Any, Any?>>

        init {
            val memberProperties = type.raw.kotlin.memberProperties
            val allProperties = type.raw.kotlin.allProperties

            this.properties = memberProperties.associateBy { it.name }
            getters =
                allProperties.filter { it.javaGetter != null }
                    .associateBy { it.javaGetter!! }
            setters =
                allProperties.mapNotNull { it as? KMutableProperty1<out Any, *> }
                    .filter { it.javaSetter != null }
                    .associateBy { it.javaSetter!! }
            propertyValidators =
                memberProperties.mapNotNull { (it as? KMutableProperty1<out Any, *>) }
                    .filter { it.javaSetter != null }
                    .associate {
                        it.name to
                            listOfNotNull(
                                it.javaSetter!!.getAnnotation(PropertyValidation::class.java)?.resolve(),
                                *(
                                    it.javaSetter!!.getAnnotation(PropertyValidations::class.java)?.validations?.map { it.resolve() }
                                        ?.toTypedArray()
                                        ?: arrayOf<PropertyValidating>()
                                ),
                            )
                    }
            dtoValidators =
                listOfNotNull(
                    type.raw.kotlin.findAnnotation<DtoValidation>()?.resolve(),
                    *(
                        type.raw.kotlin.findAnnotation<DtoValidations>()?.validations?.map { it.resolve() }?.toTypedArray()
                            ?: arrayOf<DtoValidating>()
                    ),
                )
            getterHooks =
                memberProperties.filter { it.javaGetter != null }
                    .associate {
                        it.name to
                            listOfNotNull(
                                it.javaGetter!!.getAnnotation(PropertyHook::class.java)?.resolve(),
                                *(
                                    it.javaGetter!!.getAnnotation(PropertyHooks::class.java)?.hooks?.map { it.resolve() }
                                        ?.toTypedArray()
                                        ?: arrayOf<PropertyHooking>()
                                ),
                            )
                    }
            setterHooks =
                memberProperties.mapNotNull { it as? KMutableProperty1<*, *> }
                    .filter { it.javaSetter != null }
                    .associate {
                        it.name to
                            listOfNotNull(
                                it.javaSetter!!.getAnnotation(PropertyHook::class.java)?.resolve(),
                                *(
                                    it.javaSetter!!.getAnnotation(PropertyHooks::class.java)?.hooks?.map { it.resolve() }
                                        ?.toTypedArray()
                                        ?: arrayOf<PropertyHooking>()
                                ),
                            )
                    }
            defaultValue =
                memberProperties.filter { it.javaGetter != null }
                    .associate {
                        it.name to it.javaGetter!!.getAnnotation(DefaultValue::class.java)?.resolve()
                    }
            notNullProperties =
                memberProperties.filter {
                    !it.returnType.isMarkedNullable &&
                        it.javaGetter?.getAnnotation(MetaProperty::class.java) == null &&
                        it.javaGetter?.getAnnotation(NullableProperty::class.java) == null &&
                        it.javaGetter?.getAnnotation(DefaultValue::class.java) == null
                }.toSet()
        }
    }

    fun get(type: SimpleType): CachedReflection {
        return cache.getOrPut(type) {
            CachedReflectionImpl(type)
        }
    }
}
