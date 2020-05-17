package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.google.protobuf.CodedInputStream
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.LinkedList

abstract class ProtoSupport<T : Message<T, TM>, TM : MutableMessage<T, TM>>(val fullName: String) : Metadata.BinaryMarshaller<T>, MethodDescriptor.Marshaller<T> {
    abstract val descriptor: DescriptorProto
    // val fields: List<DescriptorProtos.FieldDescriptorProto> = _fields

    @InternalProtoApi
    abstract fun newMutable(): TM

    open val fieldDescriptors: List<FieldDescriptorProto> by lazy {
        descriptor.field + extensions.flatMap { it.extendedFields }
    }

    private val fieldNameMap: Map<String, FieldDescriptorProto> by lazy {
        fieldDescriptors.associateBy { it.name } + fieldDescriptors.associateBy { it.jsonName }
    }

    private val fieldNumberMap: Map<Int, FieldDescriptorProto> by lazy {
        fieldDescriptors.associateBy { it.number }
    }

    fun fieldInfo(name: String): FieldDescriptorProto? {
        if (!name.contains('.')) {
            return fieldNameMap[name]
        }

        var target: ProtoSupport<*, *>? = this
        var result: FieldDescriptorProto? = null

        val fieldPart = LinkedList(name.split('.'))

        while (fieldPart.isNotEmpty()) {
            val field = fieldPart.poll() ?: break

            target ?: throw IllegalStateException("Nested property must be message")
            result = target.fieldInfo(field) ?: return null

            if (result.type != FieldDescriptorProto.Type.MESSAGE) {
                target = null
            } else {
                target = ProtoTypes.ensureSupportByProtoName(result.typeName)
                if (target.descriptor.options?.mapEntry == true) {
                    val mapField = fieldPart.poll() ?: break
                    result = target.fieldInfo("value") ?: return null
                    if (result.type != FieldDescriptorProto.Type.MESSAGE) {
                        target = null
                    } else {
                        target = ProtoTypes.ensureSupportByProtoName(result.typeName)
                    }
                }
            }
        }

        return result
    }

    fun fieldInfo(number: Int): FieldDescriptorProto? {
        return fieldNumberMap[number]
    }

    override fun toBytes(value: T): ByteArray {
        return value.toProto()
    }

    override fun parseBytes(serialized: ByteArray): T {
        return parse(serialized)
    }

    override fun parse(stream: InputStream): T {
        return parse(stream, Int.MAX_VALUE)
    }

    override fun stream(value: T): InputStream {
        return ByteArrayInputStream(value.toProto())
    }

    fun parse(input: InputStream, size: Int): T {
        return parse(CodedInputStream.newInstance(input), size)
    }

    fun parse(data: ByteArray, from: Int = 0, to: Int = data.size): T {
        return parse(CodedInputStream.newInstance(data, from, to - from), to - from)
    }

    @OptIn(InternalProtoApi::class)
    fun parse(input: CodedInputStream, size: Int): T {
        return newMutable().apply { readFrom(input, size) } as T
    }

    @OptIn(InternalProtoApi::class)
    fun parse(input: CodedInputStream, size: Int, block: TM.() -> Unit): T {
        return newMutable().apply {
            readFrom(input, size)
            block()
        } as T
    }

    private val _extensions = mutableListOf<ExtensionSupport<*, *>>()
    val extensions: List<ExtensionSupport<*, *>> = _extensions

    fun registerExtension(support: ExtensionSupport<*, *>) {
        _extensions.add(support)
    }
}
