package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import java.nio.charset.Charset

class EnumConverter constructor(val type: JavaType) : ValueConverter<Any> {
    companion object {
        inline operator fun <reified T> invoke(): EnumConverter {
            return EnumConverter(TypeFactory.defaultInstance().constructType(object : TypeReference<T>() {}))
        }
    }

    override fun convert(value: Any): ByteArray {
        return (value as Enum<*>).name.toByteArray()
    }

    override fun convertBack(value: ByteArray): Any {
        val stringValue = value.toString(Charset.defaultCharset())
        return type.rawClass.enumConstants.firstOrNull { (it as Enum<*>).name == stringValue }
            ?: throw RuntimeException("Invalid enum value('$stringValue') for type '${type.typeName}'.")
    }
}
