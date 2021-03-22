package com.bybutter.sisyphus.api

import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.CustomProtoTypeSupport
import com.google.api.pathtemplate.PathTemplate

interface ResourceName : CustomProtoType<String> {
    fun template(): PathTemplate

    fun support(): ResourceNameSupport<out ResourceName>

    fun singular(): String

    fun plural(): String

    fun toMap(): Map<String, String>

    operator fun get(key: String): String?
}

abstract class AbstractResourceName(
    private val data: Map<String, String>
) : ResourceName {
    override fun toMap(): Map<String, String> {
        return data
    }

    override fun get(key: String): String? {
        return data[key]
    }

    override fun plural(): String {
        return support().plural
    }

    override fun singular(): String {
        return support().singular
    }

    override fun value(): String {
        return template().instantiate(data)
    }

    override fun toString(): String {
        return value()
    }

    override fun hashCode(): Int {
        return value().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ResourceName) return false
        return other.value() == value()
    }
}

abstract class ResourceNameSupport<T : ResourceName> : CustomProtoTypeSupport<T, String> {
    override val rawType: Class<String>
        get() = String::class.java

    abstract val type: String

    abstract val patterns: List<PathTemplate>

    abstract val plural: String

    abstract val singular: String

    abstract fun tryCreate(name: String): T?

    fun matches(name: String): Boolean {
        return patterns.any {
            it.matches(name)
        }
    }
}
