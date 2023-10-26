package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.AbstractMutableMessage
import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

class DynamicMessage(
    private val support: DynamicMessageSupport,
) : AbstractMutableMessage<DynamicMessage, DynamicMessage>() {
    private val fields = mutableMapOf<Int, DynamicField<*>>()

    override fun computeHashCode(): Int {
        var result = support.hashCode()
        fields.values.forEach {
            result = result * 31 + it.hashCode()
        }
        return result
    }

    override fun equalsMessage(other: DynamicMessage): Boolean {
        if (support().name != other.support().name) return false
        return fields.contentEquals(other.fields)
    }

    override fun writeFields(writer: Writer) {
        fields.forEach { (_, value) ->
            if (value.has()) value.writeTo(writer)
        }
    }

    private fun <T> ensure(fieldName: String): DynamicField<T> {
        val field = support().fieldInfo(fieldName) ?: throw IllegalArgumentException("Unknown field $fieldName")
        return fields.getOrPut(field.number) {
            DynamicField(support, field)
        } as DynamicField<T>
    }

    private fun <T> ensure(fieldNumber: Int): DynamicField<T> {
        val field = support().fieldInfo(fieldNumber) ?: throw IllegalArgumentException("Unknown field $fieldNumber")
        return fields.getOrPut(field.number) {
            DynamicField(support, field)
        } as DynamicField<T>
    }

    override fun <T> getFieldInCurrent(fieldName: String): T {
        return ensure<T>(fieldName).get()
    }

    override fun <T> getFieldInCurrent(fieldNumber: Int): T {
        return ensure<T>(fieldNumber).get()
    }

    override fun hasFieldInCurrent(fieldName: String): Boolean {
        return ensure<Any?>(fieldName).has()
    }

    override fun hasFieldInCurrent(fieldNumber: Int): Boolean {
        return ensure<Any?>(fieldNumber).has()
    }

    @InternalProtoApi
    override fun readField(
        reader: Reader,
        field: Int,
        wire: Int,
    ): Boolean {
        support().fieldInfo(field) ?: return false
        ensure<Any?>(field).read(reader, field, wire)
        return true
    }

    override fun <T> setFieldInCurrent(
        fieldName: String,
        value: T,
    ) {
        ensure<T>(fieldName).apply {
            clearOneof(this.descriptor())
            set(value)
        }
    }

    override fun <T> setFieldInCurrent(
        fieldNumber: Int,
        value: T,
    ) {
        ensure<T>(fieldNumber).apply {
            clearOneof(this.descriptor())
            set(value)
        }
    }

    private fun clearOneof(field: FieldDescriptorProto) {
        if (!field.hasOneofIndex()) return
        val oneof = field.oneofIndex

        support().fieldDescriptors.forEach {
            if (it.hasOneofIndex() && it.oneofIndex == oneof && it.number != field.number) {
                fields.remove(it.number)
            }
        }
    }

    override fun clearFieldInCurrent(fieldName: String): Any? {
        return ensure<Any?>(fieldName).clear()
    }

    override fun clearFieldInCurrent(fieldNumber: Int): Any? {
        return ensure<Any?>(fieldNumber).clear()
    }

    override fun support(): MessageSupport<DynamicMessage, DynamicMessage> {
        return support
    }

    @InternalProtoApi
    override fun cloneMutable(): DynamicMessage {
        return support().newMutable().apply {
            mergeWith(this)
        }
    }

    override fun clear() {
        return fields.clear()
    }

    override fun mergeWith(other: DynamicMessage?) {
        if (other?.support() != support()) return
        val proto = other.toProto()
        readFrom(Reader(proto.inputStream()), proto.size)
    }
}
