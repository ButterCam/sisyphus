package com.bybutter.sisyphus.reflect

import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

class OutWildcardType private constructor(target: List<SimpleType>) : BaseWildcardType(target) {
    companion object {
        private val cache = ConcurrentHashMap<String, OutWildcardType>()

        operator fun invoke(target: List<SimpleType>): OutWildcardType {
            return cache.getOrPut(target.asSequence().map { it.typeName }.sorted().joinToString()) {
                OutWildcardType(target)
            }
        }

        val STAR = invoke(listOf())
    }

    override fun getLowerBounds(): Array<Type> {
        return arrayOf()
    }

    override fun getUpperBounds(): Array<Type> {
        return target.toTypedArray()
    }

    override fun toString(): String {
        if (target.isEmpty() || target[0] == SimpleType(Any::class.java)) {
            return "?"
        }

        return "? extends ${target.joinToString(" & ") { it.typeName }}"
    }
}
