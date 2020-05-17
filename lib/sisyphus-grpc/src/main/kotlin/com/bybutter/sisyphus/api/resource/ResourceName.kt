package com.bybutter.sisyphus.api.resource

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.CustomProtoTypeSupport
import io.grpc.Metadata
import java.lang.reflect.Proxy

interface ResourceName : CustomProtoType<String> {
    fun singular(): String
    fun plural(): String
    fun endpoint(): String?
    fun template(): PathTemplate

    fun toMap(): Map<String, String>
    fun toMutableMap(): MutableMap<String, String>

    operator fun contains(key: String): Boolean
    operator fun iterator(): Iterator<Map.Entry<String, String>>
    operator fun get(key: String): String?
    operator fun plus(map: Map<String, String>): ResourceName

    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    companion object : CustomProtoTypeSupport<ResourceName, String> {
        override val rawType: Class<String> = String::class.java

        const val WILDCARD_PART = "-"

        override fun wrapRaw(value: String): ResourceName {
            return UnknownResourceName(value)
        }

        inline operator fun <reified T : ResourceName> invoke(path: String): T {
            return UnknownResourceName<T>(path, T::class.java)
        }
    }
}

abstract class AbstractResourceName(
    private val data: Map<String, String>,
    private val template: PathTemplate,
    private val support: ResourceNameSupport<out ResourceName>
) : ResourceName {
    private val name: String by lazy {
        template().instantiate(data)
    }

    private val bytes: ByteArray by lazy {
        name.toByteArray()
    }

    override fun toString(): String {
        return name
    }

    override fun plural(): String {
        return support.plural
    }

    override fun singular(): String {
        return support.singular
    }

    override fun template(): PathTemplate {
        return template
    }

    override fun endpoint(): String? {
        return data[PathTemplate.HOSTNAME_VAR]
    }

    override fun raw(): String {
        return name
    }

    override fun contains(key: String): Boolean {
        return data.containsKey(key)
    }

    override fun iterator(): Iterator<Map.Entry<String, String>> {
        return data.iterator()
    }

    override fun get(key: String): String? {
        return data[key]
    }

    override fun toMap(): Map<String, String> {
        return data
    }

    override fun toMutableMap(): MutableMap<String, String> {
        return data.toMutableMap()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as AbstractResourceName

        if (template() != other.template()) return false
        if (!data.contentEquals(other.data)) return false
        if (endpoint() != other.endpoint()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = this.javaClass.hashCode()
        result = result * 31 + template().hashCode()
        for ((key, value) in data) {
            result = result * 31 + key.hashCode()
            result = result * 31 + value.hashCode()
        }
        return result
    }
}

abstract class ResourceNameSupport<T : ResourceName> : Metadata.AsciiMarshaller<T>, CustomProtoTypeSupport<T, String> {
    abstract val type: String

    abstract val patterns: List<PathTemplate>

    abstract val plural: String

    abstract val singular: String

    override val rawType: Class<String> = String::class.java

    override fun wrapRaw(value: String): T {
        return invoke(value)
    }

    abstract operator fun invoke(path: String): T

    fun matches(path: String): Boolean {
        return patterns.any {
            it.matches(path)
        }
    }

    fun tryCreate(path: String): T? {
        val result = invoke(path)
        return if (Proxy.isProxyClass(result.javaClass)) {
            null
        } else {
            result
        }
    }

    override fun parseAsciiString(serialized: String): T {
        return invoke(serialized)
    }

    override fun toAsciiString(value: T): String {
        return value.toString()
    }
}
