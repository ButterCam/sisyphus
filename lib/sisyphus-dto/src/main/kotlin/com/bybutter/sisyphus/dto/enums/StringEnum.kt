package com.bybutter.sisyphus.dto.enums

import com.bybutter.sisyphus.reflect.uncheckedCast
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory

interface StringEnum {
    val value: String

    companion object {
        fun <T> valueOf(value: String?, type: Class<T>): T? where T : StringEnum {
            return valueOf(value, TypeFactory.defaultInstance().constructType(type))
        }

        fun <T> valueOf(value: String?, type: TypeReference<T>): T? where T : StringEnum {
            return valueOf(value, TypeFactory.defaultInstance().constructType(type))
        }

        fun <T> valueOf(value: String?, type: JavaType): T? where T : StringEnum {
            val values = type.rawClass.enumConstants.map { it.uncheckedCast<T>() }
            return values.firstOrNull { it.value == value } ?: run {
                type.rawClass.declaredFields.filter { it.isEnumConstant && it.getDeclaredAnnotation(UnknownValue::class.java) != null }
                    .map { it.get(null).uncheckedCast<T>() }.firstOrNull()
            }
        }

        inline fun <reified T> valueOf(value: String?): T? where T : StringEnum {
            return valueOf(value, T::class.java)
        }

        inline operator fun <reified T> invoke(value: String?): T where T : StringEnum {
            return valueOf(value)
                ?: throw IllegalArgumentException("Can't found value($value) for string enum(${T::class.java.name}).")
        }

        operator fun <T> invoke(value: String?, type: Class<T>): T where T : StringEnum {
            return valueOf(value, type)
                ?: throw IllegalArgumentException("Can't found value($value) for string enum(${type.name}).")
        }
    }
}
