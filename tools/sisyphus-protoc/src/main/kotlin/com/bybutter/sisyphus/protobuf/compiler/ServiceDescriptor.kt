package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

class ServiceDescriptor(
    override val parent: FileDescriptor,
    override val descriptor: DescriptorProtos.ServiceDescriptorProto,
) : DescriptorNode<DescriptorProtos.ServiceDescriptorProto>() {
    override fun resolveChildren(children: MutableList<DescriptorNode<*>>) {
        children +=
            descriptor.methodList.map {
                MethodDescriptor(this, it)
            }
        super.resolveChildren(children)
    }

    val methods: List<MethodDescriptor> get() = children().filterIsInstance<MethodDescriptor>()

    fun fullProtoName(): String {
        return ".${file().descriptor.`package`}.${descriptor.name}"
    }

    fun name(): String {
        return descriptor.name
    }

    fun className(): ClassName {
        return ClassName(file().packageName(), name())
    }

    fun supportName(): String {
        return "${descriptor.name}Support"
    }

    fun supportClassName(): ClassName {
        return ClassName(file().internalPackageName(), supportName())
    }

    fun path(): List<Int> {
        return listOf(
            DescriptorProtos.FileDescriptorProto.SERVICE_FIELD_NUMBER,
            file().descriptor.serviceList.indexOf(descriptor),
        )
    }

    fun document(): String {
        return escapeDoc(
            file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
                it.pathList.contentEquals(path())
            }?.leadingComments ?: "",
        )
    }
}
