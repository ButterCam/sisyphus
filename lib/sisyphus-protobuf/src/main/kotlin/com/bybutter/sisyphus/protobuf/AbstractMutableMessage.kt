package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.WireType
import com.bybutter.sisyphus.protobuf.json.JsonReader
import com.bybutter.sisyphus.protobuf.json.readRaw
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FloatValue
import com.bybutter.sisyphus.protobuf.primitives.Int32Value
import com.bybutter.sisyphus.protobuf.primitives.Int64Value
import com.bybutter.sisyphus.protobuf.primitives.StringValue
import com.bybutter.sisyphus.protobuf.primitives.UInt32Value
import com.bybutter.sisyphus.protobuf.primitives.UInt64Value

abstract class AbstractMutableMessage<T : Message<T, TM>, TM : MutableMessage<T, TM>> :
    AbstractMessage<T, TM>(),
    MutableMessage<T, TM> {

    private var unknownFields: UnknownFields? = null

    private var extensions: MutableMap<Int, MessageExtension<*>>? = null

    private inline fun unknownFields(block: UnknownFields.() -> Unit) {
        (this.unknownFields ?: UnknownFields()).apply {
            block()
            unknownFields = this
        }
    }

    override fun unknownFields(): UnknownFields {
        return unknownFields ?: UnknownFields.empty
    }

    private inline fun extensions(block: MutableMap<Int, MessageExtension<*>>.() -> Unit) {
        (this.extensions ?: mutableMapOf()).apply {
            block()
            extensions = this
        }
    }

    override fun extensions(): Map<Int, MessageExtension<*>> {
        return extensions ?: mapOf()
    }

    @OptIn(InternalProtoApi::class)
    override fun readFrom(reader: Reader, size: Int) {
        val current = reader.readBytes
        while (!reader.isAtEnd && reader.readBytes - current < size) {
            val tag = reader.tag()
            val number = WireType.getFieldNumber(tag)
            val wireType = WireType.getWireType(tag).ordinal
            if (!readField(reader, number, wireType)) {
                val extension = support().extensions.firstOrNull {
                    it.number == number
                } as? ExtensionSupport<Any>

                if (extension == null) {
                    unknownFields {
                        readFrom(reader, number, wireType)
                    }
                } else {
                    extensions {
                        this[number] = extension.read(reader, number, wireType, this[number] as? MessageExtension<Any>)
                    }
                }
            }
        }

        if (size != Int.MAX_VALUE && reader.readBytes - current != size) {
            throw IllegalStateException("Wrong message data at position $current with length $size.")
        }
    }

    override fun readFrom(reader: Reader) {
        return readFrom(reader, reader.int32())
    }

    override fun readFrom(reader: JsonReader) {
        readRaw(reader)
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

    override fun <T> set(fieldNumber: Int, value: T) {
        return setFieldInCurrent(fieldNumber, value)
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

    override fun clear(fieldNumber: Int): Any? {
        return clearFieldInCurrent(fieldNumber)
    }

    protected fun <T> setFieldInExtensions(name: String, value: T) {
        val fieldNumber = support().fieldInfo(name)?.number
            ?: throw IllegalArgumentException("Message not contains field definition of '$name'.")
        setFieldInExtensions(fieldNumber, value)
    }

    protected fun <T> setFieldInExtensions(number: Int, value: T) {
        if (value == null) {
            clearFieldInCurrent(number)
            return
        }
        val extension = support().extensions.firstOrNull { it.descriptor.number == number } as? ExtensionSupport<T>
            ?: throw IllegalArgumentException("Message not contains field definition of '$number'.")
        extensions {
            this[number] = extension.wrap(value)
        }
    }

    protected fun clearFieldInExtensions(name: String): Any? {
        val fieldNumber = fieldDescriptor(name).number

        return clearFieldInExtensions(fieldNumber)
    }

    protected fun clearFieldInExtensions(number: Int): Any? {
        if (extensions()[number] == null) return null
        extensions {
            return this[number]?.value?.also {
                this.remove(number)
            }
        }
        return null
    }

    protected fun clearAllFieldInExtensions() {
        if (extensions().isEmpty()) return
        extensions {
            clear()
        }
    }

    private fun copyFrom(message: Message<*, *>, keepOriginalValues: Boolean = false) {
        for (source in message.support().fieldDescriptors) {
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

            if (target.type == FieldDescriptorProto.Type.MESSAGE && target.typeName == WellKnownTypes.ANY_TYPENAME) {
                if (target.label == FieldDescriptorProto.Label.REPEATED) {
                    this[target.name] = if (source.label == FieldDescriptorProto.Label.REPEATED) {
                        message.get<List<Any>>(source.name).mapNotNull {
                            buildTypeMessage(source.type, it)
                        }
                    } else {
                        listOf(buildTypeMessage(source.type, message[source.name]) ?: continue)
                    }
                    continue
                }
                if (source.label != FieldDescriptorProto.Label.REPEATED) {
                    this[target.name] = buildTypeMessage(source.type, message[source.name]) ?: continue
                }
            }
        }
    }

    private fun buildTypeMessage(type: FieldDescriptorProto.Type, value: Any): Message<*, *>? {
        return when (type) {
            FieldDescriptorProto.Type.DOUBLE -> DoubleValue { this.value = value as Double }
            FieldDescriptorProto.Type.FLOAT -> FloatValue { this.value = value as Float }
            FieldDescriptorProto.Type.INT64 -> Int64Value { this.value = value as Long }
            FieldDescriptorProto.Type.UINT64 -> UInt64Value { this.value = value as ULong }
            FieldDescriptorProto.Type.INT32 -> Int32Value { this.value = value as Int }
            FieldDescriptorProto.Type.BOOL -> BoolValue { this.value = value as Boolean }
            FieldDescriptorProto.Type.STRING -> StringValue { this.value = value as String }
            FieldDescriptorProto.Type.BYTES -> BytesValue { this.value = value as ByteArray }
            FieldDescriptorProto.Type.UINT32 -> UInt32Value { this.value = value as UInt }
            else -> null
        }
    }

    @InternalProtoApi
    protected abstract fun readField(reader: Reader, field: Int, wire: Int): Boolean

    protected abstract fun <T> setFieldInCurrent(fieldName: String, value: T)

    protected abstract fun <T> setFieldInCurrent(fieldNumber: Int, value: T)

    protected abstract fun clearFieldInCurrent(fieldName: String): Any?

    protected abstract fun clearFieldInCurrent(fieldNumber: Int): Any?
}
