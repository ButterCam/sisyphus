package com.bybutter.sisyphus.reflect

import java.util.concurrent.ConcurrentHashMap

open class SimpleType protected constructor(val raw: Class<*>) : JvmType() {
    companion object {
        private val cache = ConcurrentHashMap<Class<*>, SimpleType>()

        operator fun invoke(raw: Class<*>): SimpleType {
            return cache.getOrPut(raw) {
                SimpleType(raw)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return raw == (other as? SimpleType)?.raw
    }

    override fun hashCode(): Int {
        return raw.hashCode()
    }

    override fun toString(): String {
        return raw.typeName
    }
}
