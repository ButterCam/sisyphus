package com.bybutter.sisyphus.middleware.hbase.annotation

import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.CLASS)
annotation class HColumn(
    val name: String = "",
    val byteName: ByteArray = [],
    val qualifier: String = "",
    val byteQualifier: ByteArray = [],
    val converter: KClass<*> = ValueConverter::class
)
