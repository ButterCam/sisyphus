package com.bybutter.sisyphus.dto

import com.bybutter.sisyphus.dto.enums.IntEnum
import com.bybutter.sisyphus.dto.enums.StringEnum
import com.bybutter.sisyphus.reflect.uncheckedCast
import com.bybutter.sisyphus.security.base64Decode
import com.bybutter.sisyphus.security.base64UrlSafeDecode
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * The provider which can convert string value to real type value.
 */
interface DefaultValueProvider<T> {
    fun getValue(proxy: ModelProxy, param: String, property: KProperty<T?>): T?

    object Default : DefaultValueProvider<Any?> {
        override fun getValue(
            proxy: ModelProxy,
            param: String,
            property: KProperty<Any?>
        ): Any? {
            return when (property.returnType.classifier) {
                Long::class -> {
                    if (param.isEmpty()) {
                        0L
                    } else {
                        param.toLong()
                    }
                }
                Int::class -> {
                    if (param.isEmpty()) {
                        0
                    } else {
                        param.toInt()
                    }
                }
                Short::class -> {
                    if (param.isEmpty()) {
                        0.toShort()
                    } else {
                        param.toShort()
                    }
                }
                Byte::class -> {
                    if (param.isEmpty()) {
                        0.toByte()
                    } else {
                        param.toByte()
                    }
                }
                ULong::class -> {
                    if (param.isEmpty()) {
                        0.toULong()
                    } else {
                        param.toULong()
                    }
                }
                UInt::class -> {
                    if (param.isEmpty()) {
                        0.toUInt()
                    } else {
                        param.toUInt()
                    }
                }
                UShort::class -> {
                    if (param.isEmpty()) {
                        0.toUShort()
                    } else {
                        param.toUShort()
                    }
                }
                UByte::class -> {
                    if (param.isEmpty()) {
                        0.toUByte()
                    } else {
                        param.toUByte()
                    }
                }
                Char::class -> {
                    if (param.isEmpty()) {
                        0.toChar()
                    } else {
                        param[0]
                    }
                }
                Boolean::class -> {
                    if (param.isEmpty()) {
                        false
                    } else {
                        param.toBoolean()
                    }
                }
                Float::class -> {
                    if (param.isEmpty()) {
                        0.0f
                    } else {
                        param.toFloat()
                    }
                }
                Double::class -> {
                    if (param.isEmpty()) {
                        0.0
                    } else {
                        param.toDouble()
                    }
                }
                String::class -> {
                    param
                }
                ByteArray::class -> {
                    if (param.contains("""[+/]""".toRegex())) {
                        param.base64Decode()
                    } else {
                        param.base64UrlSafeDecode()
                    }
                }
                MutableList::class -> {
                    if (param.isEmpty()) {
                        mutableListOf<Any>()
                    } else {
                        throw UnsupportedOperationException("Unsupported default value type(${property.returnType})(${proxy.`$type`}).")
                    }
                }
                MutableMap::class -> {
                    if (param.isEmpty()) {
                        mutableMapOf<Any, Any>()
                    } else {
                        throw UnsupportedOperationException("Unsupported default value type(${property.returnType})(${proxy.`$type`}).")
                    }
                }
                List::class -> {
                    if (param.isEmpty()) {
                        emptyList<Any>()
                    } else {
                        throw UnsupportedOperationException("Unsupported default value type(${property.returnType})(${proxy.`$type`}).")
                    }
                }
                Map::class -> {
                    if (param.isEmpty()) {
                        emptyMap<Any, Any>()
                    } else {
                        throw UnsupportedOperationException("Unsupported default value type(${property.returnType})(${proxy.`$type`}).")
                    }
                }
                else -> {
                    when {
                        IntEnum::class.isSuperclassOf(property.returnType.classifier.uncheckedCast()) -> {
                            if (param.toIntOrNull() != null) {
                                IntEnum.valueOf(param.toInt(), property.returnType.jvmErasure.java.uncheckedCast<Class<IntEnum>>())
                            } else {
                                property.returnType.jvmErasure.java.enumConstants.firstOrNull {
                                    (it as Enum<*>).name == param
                                }
                            }
                        }
                        StringEnum::class.isSuperclassOf(property.returnType.classifier.uncheckedCast()) -> {
                            StringEnum.valueOf(param, property.returnType.jvmErasure.java.uncheckedCast<Class<StringEnum>>())
                                    ?: property.returnType.jvmErasure.java.enumConstants.firstOrNull {
                                        (it as Enum<*>).name == param
                                    }
                        }
                        else -> throw UnsupportedOperationException("Unsupported default value type(${property.returnType})(${proxy.`$type`}).")
                    }
                }
            }
        }
    }
}
