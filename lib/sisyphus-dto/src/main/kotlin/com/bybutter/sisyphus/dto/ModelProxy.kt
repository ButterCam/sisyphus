package com.bybutter.sisyphus.dto

import com.bybutter.sisyphus.reflect.SimpleType
import com.bybutter.sisyphus.reflect.uncheckedCast
import java.lang.ref.SoftReference
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

open class ModelProxy constructor(
    override val `$type`: SimpleType
) : InvocationHandler, DtoMeta, DtoModel, CachedReflection by ReflectionCache.get(`$type`) {
    private var target: SoftReference<Any>? = null

    override var `$modelMap`: MutableMap<String, Any?> = mutableMapOf()

    override var `$outputType`: Boolean = jsonTypeOutputHandler()

    override fun <T> get(name: String): T? {
        val property = properties[name]

        if (property == null) {
            return `$modelMap`[name].uncheckedCast()
        } else {
            var value: Any? = `$modelMap`.getOrElse(property.name) {
                val default = defaultValue[name] ?: return@getOrElse null
                val defaultValue = default.instance.getValue(this, default.raw.value, property)
                if (default.raw.assign) {
                    `$modelMap`[property.name] = defaultValue
                }
                defaultValue
            }

            val hooks = getterHooks[name] ?: emptyList()

            value = hooks.fold(value) { v, it ->
                it.instance.invoke(this, v, it.raw.params, property)
            }
            return value.uncheckedCast()
        }
    }

    override fun <T> set(name: String, value: T?) {
        val property = properties[name]

        if (property == null) {
            `$modelMap`[name] = value
        } else {
            val hooks = setterHooks[name] ?: emptyList()

            val value = hooks.fold(value) { v, it ->
                it.instance.invoke(this, v, it.raw.params, property).uncheckedCast()
            }
            propertyValidators[name]?.forEach {
                it.instance.verify(this, value, it.raw.params, property)
            }

            `$modelMap`[property.name] = value
        }
    }

    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
        if (target == null) {
            target = SoftReference(proxy)
        }

        val validArgs = args ?: arrayOf()

        if (getters.containsKey(method)) {
            val property = getters[method] ?: return null
            return get(property.name)
        } else if (setters.containsKey(method)) {
            val property = setters[method] ?: return null
            set(property.name, validArgs[0])
            return null
        }

        return method.invoke(this, *validArgs)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DtoModel) {
            super.equals(Proxy.getInvocationHandler(other))
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return `$type`.toString() + "@" + Integer.toHexString(hashCode())
    }

    private var exception: Exception? = null

    val isValid: Boolean
        get() {
            verifyInternal()
            return exception == null
        }

    override fun verify() {
        verifyInternal()
        throw DtoValidateException(exception ?: return)
    }

    private fun verifyInternal() {
        val property = notNullProperties.firstOrNull {
            `$modelMap`[it.name] == null
        }

        if (property != null) {
            exception = NullPointerException("Property '$property' is null, but it be declared as not null.")
            return
        }

        for (dtoValidator in dtoValidators) {
            val ex = dtoValidator.instance.verify(target?.get() as DtoModel, dtoValidator.raw.params)
            if (ex != null) {
                exception = ex
                return
            }
        }

        for ((name, validators) in propertyValidators) {
            val property = properties.getValue(name)
            for (validator in validators) {
                val ex = validator.instance.verify(
                    this,
                    get(name),
                    validator.raw.params,
                    property
                )
                if (ex != null) {
                    exception = ex
                    return
                }
            }
        }

        exception = null
        return
    }

    companion object {
        var jsonTypeOutputHandler: () -> Boolean = {
            true
        }
    }
}
