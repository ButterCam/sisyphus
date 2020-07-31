package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import java.io.OutputStream
import kotlin.reflect.KProperty

interface Message<T : Message<T, TM>, TM : MutableMessage<T, TM>> : Cloneable {
    /**
     * Get message size in bytes.
     */
    fun size(): Int

    /**
     * Get message full type name, etc: google.protobuf.Any
     */
    fun type(): String

    /**
     * Get message type url, etc: https://type.bybutter.com/google.protobuf.Any
     */
    fun typeUrl(): String

    /**
     * Serialize message to bytes.
     */
    fun toProto(): ByteArray

    fun descriptor(): DescriptorProto

    fun fieldDescriptors(): List<FieldDescriptorProto>

    fun fieldDescriptorOrNull(fieldName: String): FieldDescriptorProto?

    fun fieldDescriptorOrNull(fieldNumber: Int): FieldDescriptorProto?

    fun fieldDescriptor(fieldName: String): FieldDescriptorProto

    fun fieldDescriptor(fieldNumber: Int): FieldDescriptorProto

    fun support(): ProtoSupport<T, TM>

    operator fun iterator(): Iterator<Pair<FieldDescriptorProto, Any?>>

    /**
     * Get field value by field/json name.
     */
    operator fun <T> get(fieldName: String): T

    /**
     * Get field value by field number.
     */
    operator fun <T> get(fieldNumber: Int): T

    /**
     * Get kotlin property info by field/json name.
     */
    fun getProperty(fieldName: String): KProperty<*>?

    /**
     * Get kotlin property info by field number.
     */
    fun getProperty(fieldNumber: Int): KProperty<*>?

    /**
     * Check if the message has a specified field, it accords to is field tag contained in the serialized message.
     *
     * * Required fields always return true.
     * * Optional fields without setting any values will return false.
     * * Optional fields which have been set value will return true.
     * * Repeated fields with the empty list will return false.
     * * Repeated fields with a non-empty list will return true.
     */
    fun has(fieldName: String): Boolean

    /**
     * Check if the message has a specified field, it accords to is field tag contained in the serialized message.
     *
     * * Required fields always return true.
     * * Optional fields without setting any values will return false.
     * * Optional fields which have been set value will return true.
     * * Repeated fields with the empty list will return false.
     * * Repeated fields with a non-empty list will return true.
     */
    fun has(fieldNumber: Int): Boolean

    /**
     * Merge another message together, it will create a copy for merging.
     */
    fun unionOf(other: T?): T

    /**
     * Create a shallow copy of message.
     */
    override fun clone(): T

    /**
     * DO NOT USE IT! It designed for internal use.
     *
     * Create a shallow mutable copy of message.
     */
    @InternalProtoApi
    fun cloneMutable(): TM

    fun writeTo(output: OutputStream)

    fun writeTo(output: CodedOutputStream)

    fun writeDelimitedTo(output: OutputStream)

    fun writeDelimitedTo(output: CodedOutputStream)

    fun unknownFields(): UnknownFields
}

@OptIn(InternalProtoApi::class)
inline operator fun <T : Message<T, TM>, TM : MutableMessage<T, TM>> Message<T, TM>.invoke(block: TM.() -> Unit): T {
    return this.cloneMutable().apply(block) as T
}

interface MutableMessage<T : Message<T, TM>, TM : MutableMessage<T, TM>> : Message<T, TM> {
    operator fun <T> set(fieldName: String, value: T)
    operator fun <T> set(fieldNumber: Int, value: T)
    fun clear(fieldName: String): Any?
    fun clear(fieldNumber: Int): Any?

    /**
     * Clear all optional and repeated fields.
     */
    fun clear()

    /**
     * Merge another message to current mutable message.
     */
    fun mergeWith(other: T?)

    /**
     * Copy another message to current mutable message.
     *
     * Will overwrite current mutable message value
     * */
    fun copyFrom(message: Message<*, *>)

    /**
     * Fill another message to current mutable message.
     *
     * Keep current mutable message value.
     * */
    fun fillFrom(message: Message<*, *>)

    fun readFrom(input: CodedInputStream, size: Int)
}
