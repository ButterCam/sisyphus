package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import java.io.InputStream
import java.util.LinkedList

abstract class MessageSupport<T : Message<T, TM>, TM : MutableMessage<T, TM>> : ProtoSupport<DescriptorProto> {
    @InternalProtoApi
    abstract fun newMutable(): TM

    val fieldDescriptors: List<FieldDescriptorProto>
        get() = descriptor.field + extensions.map { it.descriptor }

    private val _extensions = mutableListOf<ExtensionSupport<*>>()

    val extensions: List<ExtensionSupport<*>> get() = _extensions

    fun registerExtension(support: ExtensionSupport<*>) {
        _extensions.add(support)
    }

    fun fieldInfo(name: String): FieldDescriptorProto? {
        if (!name.contains('.')) {
            return fieldDescriptors.firstOrNull { it.name == name || it.jsonName == name }
        }

        var target: MessageSupport<*, *>? = this
        var result: FieldDescriptorProto? = null

        val fieldPart = LinkedList(name.split('.'))

        while (fieldPart.isNotEmpty()) {
            val field = fieldPart.poll() ?: break

            target ?: throw IllegalStateException("Nested property must be message")
            result = target.fieldInfo(field) ?: return null

            if (result.type != FieldDescriptorProto.Type.MESSAGE) {
                target = null
            } else {
                target = ProtoTypes.findSupport(result.typeName) as MessageSupport<*, *>
                if (target.descriptor.options?.mapEntry == true) {
                    val mapField = fieldPart.poll() ?: break
                    result = target.fieldInfo("value") ?: return null
                    target = if (result.type != FieldDescriptorProto.Type.MESSAGE) {
                        null
                    } else {
                        ProtoTypes.findSupport(result.typeName) as MessageSupport<*, *>
                    }
                }
            }
        }

        return result
    }

    fun fieldInfo(number: Int): FieldDescriptorProto? {
        return fieldDescriptors.firstOrNull { it.number == number }
    }

    fun parse(input: InputStream, size: Int): T {
        return parse(Reader(input), size)
    }

    fun parse(data: ByteArray, from: Int = 0, to: Int = data.size): T {
        return parse(Reader(data.inputStream(from, to - from)), to - from)
    }

    @OptIn(InternalProtoApi::class)
    fun parse(reader: Reader, size: Int): T {
        return newMutable().apply { readFrom(reader, size) } as T
    }

    @OptIn(InternalProtoApi::class)
    inline fun parse(reader: Reader, size: Int, block: TM.() -> Unit): T {
        return newMutable().apply {
            readFrom(reader, size)
            block()
        } as T
    }

    @OptIn(InternalProtoApi::class)
    operator fun invoke(): T {
        return newMutable() as T
    }

    @OptIn(InternalProtoApi::class)
    inline operator fun invoke(block: TM.() -> Unit): T {
        return newMutable().apply(block) as T
    }
}
