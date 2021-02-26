package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.dto.enums.StringEnum
import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import java.nio.charset.Charset

class StringEnumConverter constructor(val type: JavaType) : ValueConverter<StringEnum> {
    companion object {
        inline operator fun <reified T> invoke(): StringEnumConverter where T : StringEnum {
            return StringEnumConverter(TypeFactory.defaultInstance().constructType(object : TypeReference<T>() {}))
        }
    }

    override fun convert(value: StringEnum): ByteArray {
        return value.value.toByteArray()
    }

    override fun convertBack(value: ByteArray): StringEnum {
        val stringValue = value.toString(Charset.defaultCharset())
        return StringEnum.valueOf(stringValue, type)
            ?: throw RuntimeException("Invalid string enum value('$stringValue') for type '${type.typeName}'.")
    }
}
