package com.bybutter.sisyphus.jackson

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

val JavaType.beanDescription: BeanDescription
    get() = Json.mapper.deserializationConfig.introspect<BeanDescription>(this)

val Class<*>.javaType: JavaType
    get() {
        return TypeFactory.defaultInstance().constructType(this)
    }
val KClass<*>.javaType: JavaType
    get() {
        return this.java.javaType
    }
val Type.javaType: JavaType
    get() {
        return TypeFactory.defaultInstance().constructType(this)
    }
val <T : Any> T.javaType: JavaType
    get() {
        return TypeFactory.defaultInstance().constructType(this.javaClass)
    }
val JavaType.kotlinType: KType
    get() {
        val parameters = this.findTypeParameters(this.rawClass)
        return this.rawClass.kotlin.createType(parameters.map { KTypeProjection.invariant(it.kotlinType) })
    }
