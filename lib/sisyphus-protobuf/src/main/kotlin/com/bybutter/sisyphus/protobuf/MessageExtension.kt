package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.reflect.uncheckedCast

interface MessageExtension<T> {
    fun support(): ExtensionSupport<T>

    fun number(): Int

    fun name(): String

    fun jsonName(): String

    fun writeTo(writer: Writer)

    val value: T
}

class MessageExtensionImpl<T>(override val value: T, private val support: ExtensionSupport<T>) : MessageExtension<T> {
    override fun support(): ExtensionSupport<T> {
        return support
    }

    override fun number(): Int {
        return support().descriptor.number
    }

    override fun name(): String {
        return support().descriptor.name
    }

    override fun jsonName(): String {
        return support().descriptor.jsonName
    }

    override fun writeTo(writer: Writer) {
        support().write(writer, value)
    }

    override fun hashCode(): Int {
        var result = support().javaClass.hashCode()
        result = result * 37 + number()

        when (value) {
            is Map<*, *> -> value.forEach {
                result = result * 31 + it.key.hashCode()
                result = result * 31 + it.value.hashCode()
            }
            is List<*> -> value.forEach {
                result = result * 31 + it.hashCode()
            }
            else -> result = result * 31 + value.hashCode()
        }
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MessageExtensionImpl<*>) return false
        if (this.support() != other.support()) return false
        if (this.value != other.value) return false
        when (value) {
            is Map<*, *> -> value.contentEquals(other.value.uncheckedCast())
            is List<*> -> value.contentEquals(other.value as List<*>)
            else -> value == other.value
        }
        return true
    }
}
