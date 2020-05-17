package com.bybutter.sisyphus.reflect

import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Type reference for resolve java generic types.
 */
abstract class TypeReference<T> protected constructor() {
    companion object {
        private val typeCache = mutableMapOf<KClass<*>, Type>()

        inline operator fun <reified T> invoke(): TypeReference<T> {
            return object : TypeReference<T>() {}
        }
    }

    /**
     * Get the type of current reference object.
     */
    val type by lazy {
        typeCache.getOrPut(this.javaClass.kotlin) {
            this.javaClass.getTypeArgument(TypeReference::class.java, 0)
        }
    }
}

/**
 * Helper function to get type reference.
 */
inline val <reified T> T.typeReference: TypeReference<T>
    get() = TypeReference()
