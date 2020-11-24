package com.bybutter.sisyphus.middleware.hbase

import com.bybutter.sisyphus.dto.enums.StringEnum
import com.bybutter.sisyphus.middleware.hbase.converter.AnyJsonConverter
import com.bybutter.sisyphus.middleware.hbase.converter.BooleanConverter
import com.bybutter.sisyphus.middleware.hbase.converter.ByteArrayConverter
import com.bybutter.sisyphus.middleware.hbase.converter.ByteConverter
import com.bybutter.sisyphus.middleware.hbase.converter.EnumConverter
import com.bybutter.sisyphus.middleware.hbase.converter.IntConverter
import com.bybutter.sisyphus.middleware.hbase.converter.LongConverter
import com.bybutter.sisyphus.middleware.hbase.converter.ShortConverter
import com.bybutter.sisyphus.middleware.hbase.converter.StringConverter
import com.bybutter.sisyphus.middleware.hbase.converter.StringEnumConverter
import com.bybutter.sisyphus.reflect.uncheckedCast
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory

interface ValueConverter<T> {
    fun convert(value: T): ByteArray?

    fun convertBack(value: ByteArray): T
}

internal inline fun <reified T : Any> getDefaultValueConverter(): ValueConverter<T>? {
    return getDefaultValueConverter(TypeFactory.defaultInstance().constructType(object : TypeReference<T>() {}))
}

internal fun <T : Any> getDefaultValueConverter(type: JavaType): ValueConverter<T>? {
    return when {
        type.isTypeOrSubTypeOf(Byte::class.java) -> ByteConverter().uncheckedCast()
        type.isTypeOrSubTypeOf(ByteArray::class.java) -> ByteArrayConverter().uncheckedCast()
        type.isTypeOrSubTypeOf(Short::class.java) -> ShortConverter().uncheckedCast()
        type.isTypeOrSubTypeOf(Int::class.java) -> IntConverter().uncheckedCast()
        type.isTypeOrSubTypeOf(Long::class.java) -> LongConverter().uncheckedCast()
        type.isTypeOrSubTypeOf(String::class.java) -> StringConverter().uncheckedCast()
        type.isTypeOrSubTypeOf(Boolean::class.java) -> BooleanConverter().uncheckedCast()
        type.isTypeOrSubTypeOf(StringEnum::class.java) -> StringEnumConverter(type).uncheckedCast()
        type.isTypeOrSubTypeOf(Enum::class.java) -> EnumConverter(type).uncheckedCast()
        else -> AnyJsonConverter(type)
    }
}
