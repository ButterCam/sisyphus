package com.bybutter.sisyphus.reflect

import com.bybutter.sisyphus.collection.contentEquals
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

class GenericType private constructor(raw: Class<*>, private val parameters: List<JvmType>) : SimpleType(raw),
    ParameterizedType {
    companion object {
        private val cache = ConcurrentHashMap<String, GenericType>()

        operator fun invoke(raw: Class<*>, parameters: List<JvmType>): GenericType {
            return cache.getOrPut("${raw.typeName}<${parameters.joinToString(", ") { it.typeName }}>") {
                GenericType(raw, parameters)
            }
        }
    }

    override fun getRawType(): Type {
        return raw
    }

    override fun getOwnerType(): Type {
        return raw.declaringClass
    }

    override fun getActualTypeArguments(): Array<Type> {
        return parameters.toTypedArray()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GenericType) return false
        return raw == other.raw && parameters.contentEquals(other.parameters)
    }

    override fun hashCode(): Int {
        var base = super.hashCode()
        for (parameter in parameters) {
            val hash = parameter.hashCode()
            base = (base xor hash) + hash
        }
        return base
    }

    override fun toString(): String {
        return "${raw.typeName}<${parameters.joinToString(", ") { it.typeName }}>"
    }
}
