package com.bybutter.sisyphus.reflect

import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

class InWildcardType private constructor(target: List<SimpleType>) : BaseWildcardType(target) {
    companion object {
        private val cache = ConcurrentHashMap<String, InWildcardType>()

        operator fun invoke(target: List<SimpleType>): InWildcardType {
            return cache.getOrPut(target.asSequence().map { it.typeName }.sorted().joinToString()) {
                InWildcardType(target)
            }
        }
    }

    override fun getLowerBounds(): Array<Type> {
        return target.toTypedArray()
    }

    override fun getUpperBounds(): Array<Type> {
        return arrayOf()
    }

    override fun toString(): String {
        return "? super ${target.joinToString(" & ") { it.typeName }}"
    }
}
