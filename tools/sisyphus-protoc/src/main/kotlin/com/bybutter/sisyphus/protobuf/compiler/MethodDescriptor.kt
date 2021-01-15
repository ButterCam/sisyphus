package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.bybutter.sisyphus.string.toCamelCase
import com.google.protobuf.DescriptorProtos

class MethodDescriptor(
    val parent: ServiceDescriptor,
    override val descriptor: DescriptorProtos.MethodDescriptorProto
) : DescriptorNode<DescriptorProtos.MethodDescriptorProto> {
    fun name(): String {
        return descriptor.name.toCamelCase()
    }

    fun fullProtoName(): String {
        return "${parent.fullProtoName()}/${descriptor.name}"
    }

    fun path(): List<Int> {
        return parent.path() + listOf(
            DescriptorProtos.ServiceDescriptorProto.METHOD_FIELD_NUMBER,
            parent.descriptor.methodList.indexOf(descriptor)
        )
    }

    fun document(): String {
        return escapeDoc(file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
            it.pathList.contentEquals(path())
        }?.leadingComments ?: "")
    }

    fun inputMessage(): MessageDescriptor {
        return fileSet().ensureMessage(descriptor.inputType)
    }

    fun outputMessage(): MessageDescriptor {
        return fileSet().ensureMessage(descriptor.outputType)
    }
}
