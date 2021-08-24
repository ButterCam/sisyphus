package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.jackson.Json
import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory

class AnyJsonConverter<T : Any> constructor(val type: JavaType) : ValueConverter<T> {
    companion object {
        inline operator fun <reified T : Any> invoke(): AnyJsonConverter<T> {
            return AnyJsonConverter(TypeFactory.defaultInstance().constructType(object : TypeReference<T>() {}))
        }
    }

    override fun convert(value: T): ByteArray {
        return Json.serialize(value).toByteArray()
    }

    override fun convertBack(value: ByteArray): T {
        return Json.deserialize(value.toString(Charsets.UTF_8), type)
    }
}
