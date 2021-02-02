package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.reflect.uncheckedCast
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.reflect.KProperty

abstract class AbstractMessage<T : Message<T, TM>, TM : MutableMessage<T, TM>> : Message<T, TM> {
    init {
        // Initialize [ProtoTypes] class to read all proto info
        ProtoTypes
    }

    private var hashCodeCache: Int = 0

    private val _unknownFields = UnknownFields()

    @OptIn(InternalProtoApi::class)
    protected val _extensions = mutableMapOf<Int, MessageExtension<*>>()

    override fun fieldDescriptors(): List<FieldDescriptorProto> {
        return support().fieldDescriptors
    }

    override fun fieldDescriptor(fieldName: String): FieldDescriptorProto {
        return fieldDescriptorOrNull(fieldName)
            ?: throw IllegalArgumentException("Message not contains field definition of '$fieldName'.")
    }

    override fun fieldDescriptor(fieldNumber: Int): FieldDescriptorProto {
        return fieldDescriptorOrNull(fieldNumber)
            ?: throw IllegalArgumentException("Message not contains field definition of '$fieldNumber'.")
    }

    override fun fieldDescriptorOrNull(fieldName: String): FieldDescriptorProto? {
        return support().fieldInfo(fieldName)
    }

    override fun fieldDescriptorOrNull(fieldNumber: Int): FieldDescriptorProto? {
        return support().fieldInfo(fieldNumber)
    }

    override fun iterator(): Iterator<Pair<FieldDescriptorProto, Any?>> {
        return MessageIterator(this)
    }

    override fun descriptor(): DescriptorProto {
        return support().descriptor
    }

    override fun hashCode(): Int {
        var result = computeHashCode()
        for (extension in _extensions) {
            result = result * 43 + extension.hashCode()
        }

        result = result * 57 + unknownFields().hashCode()
        hashCodeCache = result
        return result
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (javaClass != other.javaClass) return false

        val message = other as AbstractMessage<*, *>

        return equalsMessage(other as T) && _extensions.contentEquals(message._extensions) && unknownFields() == message.unknownFields()
    }

    override fun type(): String {
        return support().name
    }

    override fun typeUrl(): String {
        return "types.bybutter.com/${support().name}"
    }

    override fun toProto(): ByteArray {
        return ByteArrayOutputStream().use {
            writeTo(it)
            it.toByteArray()
        }
    }

    @OptIn(InternalProtoApi::class)
    override fun clone(): T {
        return cloneMutable().uncheckedCast()
    }

    @OptIn(InternalProtoApi::class)
    override fun unionOf(other: T?): T {
        return cloneMutable().apply {
            mergeWith(other)
        }.uncheckedCast()
    }

    override fun <T> get(fieldName: String): T {
        if (!fieldName.contains('.')) {
            return getFieldInCurrent(fieldName)
        }

        var target: Any? = this
        for (field in fieldName.split('.')) {
            val current = target ?: return null as T

            target = when (current) {
                is AbstractMessage<*, *> -> current.getFieldInCurrent(field)
                is Map<*, *> -> current[field]
                else -> throw IllegalStateException("Nested property must be message")
            }
        }
        return target as T
    }

    override fun <T> get(fieldNumber: Int): T {
        return getFieldInCurrent(fieldNumber)
    }

    override fun has(fieldName: String): Boolean {
        if (!fieldName.contains('.')) {
            return hasFieldInCurrent(fieldName)
        }

        var target: Any? = this
        val fieldPart = fieldName.split('.')

        for (field in fieldPart) {
            val current = target ?: return false

            target = when (current) {
                is AbstractMessage<*, *> -> {
                    if (!current.hasFieldInCurrent(field)) {
                        return false
                    }
                    current.getFieldInCurrent(field)
                }
                is Map<*, *> -> {
                    if (!current.contains(field)) {
                        return false
                    }
                    current[field]
                }
                else -> throw IllegalStateException("Nested property must be message")
            }
        }
        return true
    }

    override fun has(fieldNumber: Int): Boolean {
        return hasFieldInCurrent(fieldNumber)
    }

    fun fieldInfo(name: String): FieldDescriptorProto? {
        return support().fieldInfo(name)
    }

    fun fieldInfo(number: Int): FieldDescriptorProto? {
        return support().fieldInfo(number)
    }

    override fun writeTo(output: OutputStream) {
        val writer = Writer()
        writeTo(writer)
        writer.writeTo(output)
    }

    override fun writeTo(writer: Writer) {
        writeFields(writer)
        for (extension in _extensions) {
            extension.value.writeTo(writer)
        }
        unknownFields().writeTo(writer)
    }

    override fun writeDelimitedTo(output: OutputStream) {
        val writer = Writer()
        writeTo(writer)
        writer.ld().writeTo(output)
    }

    protected fun <T> getFieldInExtensions(name: String): T {
        val number = support().fieldInfo(name)?.number ?: throw IllegalArgumentException("Message not contains field definition of '$name'.")
        return getFieldInExtensions(number)
    }

    protected fun <T> getFieldInExtensions(number: Int): T {
        val extensions = support().extensions.firstOrNull { it.descriptor.number == number } ?: throw IllegalArgumentException("Message not contains field definition of '$number'.")
        return (_extensions[number]?.value ?: extensions.default()).uncheckedCast()
    }

    protected fun getPropertyInExtensions(name: String): KProperty<*>? {
        val extension = support().extensions.firstOrNull { it.descriptor.name == name || it.descriptor.jsonName == name }
            ?: return null
        return extension.getProperty()
    }

    protected fun getPropertyInExtensions(number: Int): KProperty<*>? {
        val extension = support().extensions.firstOrNull { it.descriptor.number == number }
            ?: return null
        return extension.getProperty()
    }

    protected fun hasFieldInExtensions(name: String): Boolean {
        val number = support().fieldInfo(name)?.number ?: return false
        return hasFieldInExtensions(number)
    }

    protected fun hasFieldInExtensions(number: Int): Boolean {
        return _extensions[number] != null
    }

    override fun unknownFields(): UnknownFields {
        return _unknownFields
    }

    override fun extensions(): Map<Int, MessageExtension<*>> {
        return _extensions
    }

    protected abstract fun computeHashCode(): Int

    protected abstract fun equalsMessage(other: T): Boolean

    protected abstract fun writeFields(writer: Writer)

    protected abstract fun <T> getFieldInCurrent(fieldName: String): T

    protected abstract fun <T> getFieldInCurrent(fieldNumber: Int): T

    protected abstract fun hasFieldInCurrent(fieldName: String): Boolean

    protected abstract fun hasFieldInCurrent(fieldNumber: Int): Boolean
}

private class MessageIterator(private val message: Message<*, *>) : Iterator<Pair<FieldDescriptorProto, Any?>> {
    private val fieldIterator = message.fieldDescriptors().iterator()

    override fun hasNext(): Boolean {
        return fieldIterator.hasNext()
    }

    override fun next(): Pair<FieldDescriptorProto, Any?> {
        val field = fieldIterator.next()
        val value: Any? = message[field.number]

        return field to value
    }
}
