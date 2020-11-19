package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.collection.firstNotNull
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.reflect.KProperty

abstract class AbstractMessage<T : Message<T, TM>, TM : MutableMessage<T, TM>>(
    protected val _support: ProtoSupport<T, TM>
) : Message<T, TM> {
    init {
        // Initialize [ProtoTypes] class to read all proto info
        ProtoTypes
    }

    private var hashCodeCache: Int = 0

    private val _unknownFields = UnknownFields()

    @OptIn(InternalProtoApi::class)
    protected val _extensions = hashMapOf<String, AbstractMutableMessage<*, *>>()
        get() {
            if (field.size < _support.extensions.size) {
                for (extension in _support.extensions) {
                    if (!field.containsKey(extension.fullName)) {
                        field[extension.fullName] = _unknownFields.exportExtension(extension) as AbstractMutableMessage<*, *>
                    }
                }
            }
            return field
        }

    override fun fieldDescriptors(): List<FieldDescriptorProto> {
        return _support.fieldDescriptors
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
        return _support.fieldInfo(fieldName)
                ?: _extensions.values.firstNotNull { it.fieldInfo(fieldName) }
    }

    override fun fieldDescriptorOrNull(fieldNumber: Int): FieldDescriptorProto? {
        return _support.fieldInfo(fieldNumber)
                ?: _extensions.values.firstNotNull { it.fieldInfo(fieldNumber) }
    }

    fun invalidCache() {
        hashCodeCache = 0
    }

    override fun iterator(): Iterator<Pair<FieldDescriptorProto, Any?>> {
        return MessageIterator(this)
    }

    override fun descriptor(): DescriptorProto {
        return _support.descriptor
    }

    override fun support(): ProtoSupport<T, TM> {
        return _support
    }

    override fun hashCode(): Int {
        if (hashCodeCache != 0) {
            return hashCodeCache
        }

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

        return equals(other as T) && _extensions.contentEquals(message._extensions) && unknownFields() == message.unknownFields()
    }

    override fun type(): String {
        return _support.fullName
    }

    override fun typeUrl(): String {
        return "types.bybutter.com/${_support.fullName}"
    }

    override fun toProto(): ByteArray {
        return ByteArrayOutputStream().use {
            writeTo(it)
            it.toByteArray()
        }
    }

    override fun clone(): T {
        return invoke { }
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

    protected abstract fun computeHashCode(): Int

    protected abstract fun equals(other: T): Boolean

    protected abstract fun writeFields(writer: Writer)

    fun fieldInfo(name: String): FieldDescriptorProto? {
        return _support.fieldInfo(name)
    }

    fun fieldInfo(number: Int): FieldDescriptorProto? {
        return _support.fieldInfo(number)
    }

    override fun writeTo(output: OutputStream) {
        val writer = Writer()
        writeTo(writer)
        writer.writeTo(output)
    }

    override fun writeTo(writer: Writer) {
        writeFields(writer)
        for ((_, extension) in _extensions) {
            extension.writeTo(writer)
        }
        unknownFields().writeTo(writer)
    }

    override fun writeDelimitedTo(output: OutputStream) {
        val writer = Writer()
        writeTo(writer)
        writer.ld().writeTo(output)
    }

    protected abstract fun <T> getFieldInCurrent(fieldName: String): T

    protected abstract fun hasFieldInCurrent(fieldName: String): Boolean

    protected fun <T> getFieldInExtensions(name: String): T {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(name) != null }
                        ?: throw IllegalArgumentException("Message not contains field definition of '$name'.")

        return extension[name]
    }

    protected fun <T> getFieldInExtensions(number: Int): T {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(number) != null }
                        ?: throw IllegalArgumentException("Message not contains field definition of '$number'.")

        return extension[number]
    }

    protected fun getPropertyInExtensions(name: String): KProperty<*>? {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(name) != null } ?: return null

        return extension.getProperty(name)
    }

    protected fun getPropertyInExtensions(number: Int): KProperty<*>? {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(number) != null } ?: return null

        return extension.getProperty(number)
    }

    protected fun hasFieldInExtensions(name: String): Boolean {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(name) != null } ?: return false

        return extension.has(name)
    }

    protected fun hasFieldInExtensions(number: Int): Boolean {
        val extension =
                _extensions.values.firstOrNull { it.fieldInfo(number) != null } ?: return false

        return extension.has(number)
    }

    override fun unknownFields(): UnknownFields {
        return _unknownFields
    }
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
