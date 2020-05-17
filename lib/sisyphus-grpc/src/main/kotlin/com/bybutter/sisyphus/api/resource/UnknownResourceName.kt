package com.bybutter.sisyphus.api.resource

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.full.companionObjectInstance

class UnknownResourceName<T : ResourceName> private constructor(
    private val support: ResourceNameSupport<T>,
    private val name: String
) : ResourceName, InvocationHandler {

    override fun singular(): String {
        return support.singular
    }

    override fun plural(): String {
        return support.plural
    }

    override fun endpoint(): String? {
        return this[PathTemplate.HOSTNAME_VAR]
    }

    override fun template(): PathTemplate {
        return patterns[0]
    }

    override fun toMap(): Map<String, String> {
        return mapOf()
    }

    override fun toMutableMap(): MutableMap<String, String> {
        return mutableMapOf()
    }

    override fun contains(key: String): Boolean {
        return false
    }

    override fun iterator(): Iterator<Map.Entry<String, String>> {
        return emptyMap<String, String>().iterator()
    }

    override fun get(key: String): String? {
        return null
    }

    override fun plus(map: Map<String, String>): ResourceName {
        return this
    }

    override fun raw(): String {
        return name
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        return other?.toString() == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        val args = args ?: arrayOf()
        if (method.declaringClass == Any::class.java) {
            return method.invoke(this, *args)
        }

        if (method.declaringClass == ResourceName::class.java) {
            return method.invoke(this, *args)
        }

        if (Map::class.java.isAssignableFrom(method.declaringClass)) {
            return method.invoke(this, *args)
        }

        return "unknown"
    }

    companion object : ResourceNameSupport<ResourceName>() {
        override val type: String = "butterapis.com/Resource"
        override val patterns: List<PathTemplate> = listOf(PathTemplate.create("**"))
        override val plural: String = "resources"
        override val singular: String = "resource"

        override fun invoke(path: String): ResourceName {
            return UnknownResourceName(UnknownResourceName, path)
        }

        operator fun <T : ResourceName> invoke(name: String, target: Class<T>): T {
            val support = (target.kotlin.companionObjectInstance as? ResourceNameSupport<T>) ?: UnknownResourceName
            return Proxy.newProxyInstance(
                    target.classLoader,
                    arrayOf(target),
                    UnknownResourceName(support, name)
            ) as T
        }
    }
}
