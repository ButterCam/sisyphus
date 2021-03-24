package com.bybutter.sisyphus.dto

import com.bybutter.sisyphus.reflect.SimpleType
import com.bybutter.sisyphus.reflect.TypeReference
import com.bybutter.sisyphus.reflect.instance
import com.bybutter.sisyphus.reflect.jvm
import com.bybutter.sisyphus.reflect.uncheckedCast
import java.lang.reflect.Proxy
import java.util.HashMap

/**
 * [DtoModel] is the root class of DTOs, it provides the json deserializer for jackson.
 *
 * Data transfer object(DTO) is the model which is used in API request and response.
 *
 * All of DTOs is interface, all instance will be built by [ModelFactory] in JAVA or [DtoModel] in Kotlin,
 * you could be not implementing this interface, all of the logic will be handled by dynamic proxy.
 *
 * @see PropertyHook
 * @see DtoMeta
 * @see DtoModel.Companion
 */
interface DtoModel {
    /**
     * Invokable companion of [DtoModel], provide DSL for create DTO instance.
     */
    companion object {
        val proxyCache = mutableMapOf<SimpleType, Class<*>>()

        /**
         * Create DTO instance with init body.
         */
        inline operator fun <reified T : DtoModel> invoke(block: T.() -> Unit): T {
            return invoke(object : TypeReference<T>() {}, block)
        }

        /**
         * Create DTO instance.
         */
        inline operator fun <reified T : DtoModel> invoke(): T {
            return invoke(object : TypeReference<T>() {})
        }

        /**
         * Create DTO instance with shallow copy.
         */
        inline operator fun <reified T : DtoModel> invoke(model: DtoModel): T {
            return invoke {
                val map = HashMap((model as DtoMeta).`$modelMap`)
                (this as DtoMeta).`$modelMap` = map
            }
        }

        /**
         * Create DTO instance with shallow copy and init body.
         */
        inline operator fun <reified T : DtoModel> invoke(model: DtoModel, block: T.() -> Unit): T {
            return invoke {
                val map = HashMap((model as DtoMeta).`$modelMap`)
                (this as DtoMeta).`$modelMap` = map
                block(this)
            }
        }

        operator fun <T : DtoModel> invoke(typeReference: TypeReference<T>): T {
            return invoke(typeReference.type.jvm as SimpleType)
        }

        inline operator fun <T : DtoModel> invoke(typeReference: TypeReference<T>, block: T.() -> Unit): T {
            return invoke(typeReference.type.jvm as SimpleType, block)
        }

        operator fun <T : DtoModel> invoke(type: SimpleType): T {
            val value = (
                proxyCache[type]?.instance() ?: Proxy.newProxyInstance(
                    type.raw.classLoader,
                    arrayOf(type.raw, DtoMeta::class.java),
                    ModelProxy(type)
                )
                ).uncheckedCast<T>()
            value.verify()
            return value
        }

        inline operator fun <T : DtoModel> invoke(
            type: SimpleType,
            block: T.() -> Unit
        ): T {
            val value = (
                proxyCache[type]?.instance() ?: Proxy.newProxyInstance(
                    type.raw.classLoader,
                    arrayOf(type.raw, DtoMeta::class.java),
                    ModelProxy(type)
                )
                ).uncheckedCast<T>()
            value.apply(block)
            value.verify()
            return value
        }
    }

    fun verify()
}

val DtoModel.isValid: Boolean
    get() {
        val proxy = Proxy.getInvocationHandler(this) as ModelProxy
        return proxy.isValid
    }

/**
 * Cast a DTO instance to other type, all DTO can be cast to each other.
 *
 * **Attention! The result of [this method][cast] is not the origin object, but origin object and result object will share the structure of content.**
 */
inline fun <reified T : DtoModel> DtoModel.cast(noinline apply: T.() -> Unit = {}): T {
    return DtoModel(TypeReference<T>()) {
        this.uncheckedCast<DtoMeta>().`$modelMap` = this@cast.uncheckedCast<DtoMeta>().`$modelMap`
        apply(this)
    }
}

/**
 * Cast a DTO instance to other type, all DTO can be cast to each other.
 *
 * **Attention! The result of [this method][cast] is not the origin object, but origin object and result object will share the structure of content.**
 */
fun <T : DtoModel> DtoModel.castTo(clazz: Class<T>, apply: T.() -> Unit = {}): T {
    return DtoModel(clazz.jvm) {
        this.uncheckedCast<DtoMeta>().`$modelMap` = this@castTo.uncheckedCast<DtoMeta>().`$modelMap`
        apply(this)
    }
}

/**
 * Erase type info for a DTO instance, it will **not contains** `$type` property for serialized json.
 *
 * @see DtoModel.withType
 */
fun <T> T.eraseType(): T where T : DtoModel {
    this.uncheckedCast<DtoMeta>().`$outputType` = false
    return this
}

/**
 * Add type info for a DTO instance, it will **contains** `$type` property for serialized json.
 *
 * @see DtoModel.eraseType
 */
fun <T> T.withType(): T where T : DtoModel {
    this.uncheckedCast<DtoMeta>().`$outputType` = true
    return this
}

val DtoModel.proxy: ModelProxy
    get() = Proxy.getInvocationHandler(this) as ModelProxy
