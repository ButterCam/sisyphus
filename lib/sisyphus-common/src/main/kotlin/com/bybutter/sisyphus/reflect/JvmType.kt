package com.bybutter.sisyphus.reflect

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * Type to describe a JVM type.
 *
 * @see SimpleType
 * @see GenericType
 * @see InWildcardType
 * @see OutWildcardType
 */
abstract class JvmType protected constructor() : Type {
    companion object {
        fun fromType(type: Type): JvmType {
            return when (type) {
                is JvmType -> {
                    type
                }
                is Class<*> -> {
                    SimpleType(type)
                }
                is ParameterizedType -> {
                    GenericType(type.rawType as Class<*>, type.actualTypeArguments.map { fromType(it) })
                }
                is WildcardType -> {
                    if (type.lowerBounds.isNotEmpty()) {
                        InWildcardType(type.lowerBounds.map { fromType(it) as SimpleType })
                    } else {
                        OutWildcardType(type.upperBounds.map { fromType(it) as SimpleType })
                    }
                }
                else -> {
                    fromName(type.typeName)
                }
            }
        }

        fun fromName(name: String): JvmType {
            return when {
                name == "?" -> OutWildcardType.STAR
                name.startsWith("? extends ") ->
                    OutWildcardType(
                        name.substringAfter("? extends ").split(" & ").map { it.toType() as SimpleType },
                    )
                name.startsWith("? super ") ->
                    InWildcardType(
                        name.substringAfter("? extends ").split(" & ").map { it.toType() as SimpleType },
                    )
                name.indexOf('<') > 0 -> {
                    val raw = Class.forName(name.substringBefore('<'))
                    val parameters = name.substring(name.indexOf('<') + 1, name.lastIndexOf('>'))
                    GenericType(raw, splitWithLayer(parameters).map { it.toType() })
                }
                else -> SimpleType(Class.forName(name))
            }
        }

        private fun splitWithLayer(
            value: String,
            splitter: String = ", ",
            upLayer: String = "<",
            downLayer: String = ">",
        ): List<String> {
            var layer = 0
            var index = 0
            val builder = StringBuilder()
            val result = mutableListOf<String>()

            while (index < value.length) {
                if (layer < 0) {
                    throw IllegalStateException("Wrong generic type format.")
                }

                if (value.startsWith(splitter, index) && layer == 0) {
                    result.add(builder.toString())
                    builder.clear()
                    index += splitter.length
                    continue
                }

                if (value.startsWith(upLayer, index)) {
                    builder.append(upLayer)
                    layer++
                    index += upLayer.length
                    continue
                }

                if (value.startsWith(downLayer, index)) {
                    builder.append(downLayer)
                    layer--
                    index += downLayer.length
                    continue
                }

                builder.append(value[index])
                index++
            }

            result.add(builder.toString())
            return result
        }
    }
}
