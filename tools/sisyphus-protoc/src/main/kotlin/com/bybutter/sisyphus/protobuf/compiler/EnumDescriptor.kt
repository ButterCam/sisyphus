package com.bybutter.sisyphus.protobuf.compiler

import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

class EnumDescriptor(
    override val parent: DescriptorNode<*>,
    override val descriptor: DescriptorProtos.EnumDescriptorProto
) : DescriptorNode<DescriptorProtos.EnumDescriptorProto>() {
    init {
        fileSet().registerLookup(fullProtoName(), this)
    }

    override fun resolveChildren(children: MutableList<DescriptorNode<*>>) {
        children += descriptor.valueList.map {
            EnumValueDescriptor(this, it)
        }
        super.resolveChildren(children)
    }

    val values: List<EnumValueDescriptor> get() = children().filterIsInstance<EnumValueDescriptor>()

    fun name(): String {
        return descriptor.name
    }

    fun supportName(): String {
        return "${name()}Support"
    }

    fun className(): ClassName {
        return when (val parent = this.parent) {
            is MessageDescriptor -> {
                parent.className().nestedClass(name())
            }
            is FileDescriptor -> {
                ClassName(parent.packageName(), name())
            }
            else -> TODO()
        }
    }

    fun supportClassName(): ClassName {
        return when (val parent = this.parent) {
            is MessageDescriptor -> {
                parent.supportClassName().nestedClass(supportName())
            }
            is FileDescriptor -> {
                ClassName(parent.internalPackageName(), supportName())
            }
            else -> TODO()
        }
    }

    fun fullProtoName(): String {
        return when (val parent = this.parent) {
            is MessageDescriptor -> "${parent.fullProtoName()}.${descriptor.name}"
            is FileDescriptor -> ".${parent.descriptor.`package`}.${descriptor.name}"
            else -> TODO()
        }
    }
}
