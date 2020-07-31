package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.BOOL
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.BYTES
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.DOUBLE
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.FLOAT
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.INT32
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.INT64
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.MESSAGE
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.STRING
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.UINT32
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto.Type.UINT64
import com.bybutter.sisyphus.protobuf.primitives.FloatValue
import com.bybutter.sisyphus.protobuf.primitives.Int32Value
import com.bybutter.sisyphus.protobuf.primitives.Int64Value
import com.bybutter.sisyphus.protobuf.primitives.StringValue
import com.bybutter.sisyphus.protobuf.primitives.UInt32Value
import com.bybutter.sisyphus.protobuf.primitives.UInt64Value
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

    override fun copyFrom(message: Message<*, *>) {
        copyFrom(message, false)
    }

    override fun fillFrom(message: Message<*, *>) {
        copyFrom(message, true)
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

    private fun copyFrom(message: Message<*, *>, keepOriginalValues: Boolean = false) {
        for (source in message.descriptor().field) {
            val target = this.fieldDescriptorOrNull(source.name) ?: continue

            if (keepOriginalValues && this.has(source.name)) continue

            if (target.type == source.type && target.typeName == source.typeName) {
                if (target.label == source.label) {
                    this[target.name] = message.get<Any>(source.name)
                    continue
                }
                if (target.label == FieldDescriptorProto.Label.REPEATED) {
                    this[target.name] = listOf(message.get<Any>(source.name))
                    continue
                }
            }

            if (target.type == MESSAGE && target.typeName == WellKnownTypes.ANY_TYPENAME) {
                if (target.label == FieldDescriptorProto.Label.REPEATED) {
                    this[target.name] = if (source.label == FieldDescriptorProto.Label.REPEATED) {
                        message.get<List<Any>>(source.name).mapNotNull {
                            buildMessage(source.type, it)
                        }
                    } else {
                        listOf(buildMessage(source.type, message[source.name]) ?: continue)
                    }
                    continue
                }
                if (source.label != FieldDescriptorProto.Label.REPEATED) {
                    this[target.name] = buildMessage(source.type, message[source.name]) ?: continue
                }
            }
        }
    }

    private fun buildMessage(type: FieldDescriptorProto.Type, value: Any): Message<*, *>? {
        return when (type) {
            DOUBLE -> DoubleValue { this.value = value as Double }
            FLOAT -> FloatValue { this.value = value as Float }
            INT64 -> Int64Value { this.value = value as Long }
            UINT64 -> UInt64Value { this.value = value as ULong }
            INT32 -> Int32Value { this.value = value as Int }
            BOOL -> BoolValue { this.value = value as Boolean }
            STRING -> StringValue { this.value = value as String }
            BYTES -> BytesValue { this.value = value as ByteArray }
            UINT32 -> UInt32Value { this.value = value as UInt }
            else -> null
        }
    }
}
