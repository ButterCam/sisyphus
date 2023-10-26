package com.bybutter.sisyphus.dto.enums

import com.bybutter.sisyphus.reflect.uncheckedCast
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory

interface IntEnum {
    val number: Int

    companion object {
        fun <T> valueOf(
            value: Int,
            type: Class<T>,
        ): T? where T : IntEnum {
            return valueOf(value, TypeFactory.defaultInstance().constructType(type))
        }

        fun <T> valueOf(
            value: Int,
            type: TypeReference<T>,
        ): T? where T : IntEnum {
            return valueOf(value, TypeFactory.defaultInstance().constructType(type))
        }

        fun <T> valueOf(
            value: Int,
            type: JavaType,
        ): T? where T : IntEnum {
            val values = type.rawClass.enumConstants.map { it.uncheckedCast<T>() }
            return values.firstOrNull { it.number == value } ?: {
                type.rawClass.declaredFields.filter { it.isEnumConstant && it.getDeclaredAnnotation(UnknownValue::class.java) != null }
                    .map { it.get(null).uncheckedCast<T>() }.firstOrNull()
            }()
        }

        inline fun <reified T> valueOf(value: Int): T? where T : IntEnum {
            return valueOf(value, T::class.java)
        }

        inline operator fun <reified T> invoke(value: Int): T where T : IntEnum {
            return valueOf(value)
                ?: throw IllegalArgumentException("Can't found value($value) for int enum(${T::class.java.name}).")
        }

        operator fun <T> invoke(
            value: Int,
            type: Class<T>,
        ): T where T : IntEnum {
            return valueOf(value, type)
                ?: throw IllegalArgumentException("Can't found value($value) for int enum(${type.name}).")
        }
    }
}
