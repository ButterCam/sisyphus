package com.bybutter.sisyphus.protobuf

import com.google.protobuf.CodedInputStream
import com.google.protobuf.WireFormat

@OptIn(ExperimentalUnsignedTypes::class)
abstract class AbstractMutableMessage<T : Message<T, TM>, TM : MutableMessage<T, TM>>(
    support: ProtoSupport<T, TM>
) : AbstractMessage<T, TM>(support), MutableMessage<T, TM> {

    abstract fun readField(input: CodedInputStream, field: Int, wire: Int): Boolean

    override fun readFrom(input: CodedInputStream, size: Int) {
        val current = input.totalBytesRead
        while (!input.isAtEnd && input.totalBytesRead - current < size) {
            val tag = input.readTag()
            val number = WireFormat.getTagFieldNumber(tag)
            val wireType = WireFormat.getTagWireType(tag)
            if (!readField(input, number, wireType)) {
                if (!_extensions.values.any { it.readField(input, number, wireType) }) {
                    unknownFields().readFrom(input, number, wireType)
                }
            }
        }

        if (size != Int.MAX_VALUE && input.totalBytesRead - current != size) {
            throw IllegalStateException("Wrong message data at position $current with length $size.")
        }
    }

    override fun <T> set(fieldName: String, value: T) {
        if (!fieldName.contains('.')) {
            return setFieldInCurrent(fieldName, value)
        }
        throw UnsupportedOperationException("Set field not support nested field.")
    }

    override fun clear(fieldName: String): Any? {
        if (!fieldName.contains('.')) {
            return clearFieldInCurrent(fieldName)
        }

        var target: Any? = this
        val fieldPart = fieldName.split('.')

        for ((index, field) in fieldPart.withIndex()) {
            val current = target ?: return null

            target = when (current) {
                is AbstractMutableMessage<*, *> -> {
                    if (index == fieldPart.size - 1) {
                        return current.clearFieldInCurrent(field)
                    }
                    current.getFieldInCurrent(field)
                }
                is MutableMap<*, *> -> {
                    if (index == fieldPart.size - 1) {
                        return current.remove(field)
                    }
                    current[field]
                }
                else -> throw IllegalStateException("Nested property must be message")
            }
        }

        return null
    }

    protected abstract fun <T> setFieldInCurrent(fieldName: String, value: T)

    protected abstract fun clearFieldInCurrent(fieldName: String): Any?

    protected fun <T> setFieldInExtensions(name: String, value: T) {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(name) != null }
                        ?: throw IllegalArgumentException("Message not contains field definition of '$name'.")

        extension[name] = value
        invalidCache()
    }

    protected fun <T> setFieldInExtensions(number: Int, value: T) {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(number) != null }
                        ?: throw IllegalArgumentException("Message not contains field definition of '$number'.")

        extension[number] = value
        invalidCache()
    }

    protected fun clearFieldInExtensions(name: String): Any? {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(name) != null } ?: return null

        invalidCache()
        return extension.clear(name)
    }

    protected fun clearFieldInExtensions(number: Int): Any? {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(number) != null } ?: return null

        invalidCache()
        return extension.clear(number)
    }

    protected fun clearAllFieldInExtensions() {
        for ((_, extension) in _extensions) {
            extension.clear()
        }
    }
}
