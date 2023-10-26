package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

class MessageDescriptor(
    override val parent: DescriptorNode<*>,
    override val descriptor: DescriptorProtos.DescriptorProto,
) : DescriptorNode<DescriptorProtos.DescriptorProto>() {
    init {
        fileSet().registerLookup(fullProtoName(), this)
    }

    override fun resolveChildren(children: MutableList<DescriptorNode<*>>) {
        children +=
            descriptor.fieldList.map {
                MessageFieldDescriptor(this, it)
            }
        children +=
            descriptor.nestedTypeList.map {
                MessageDescriptor(this, it)
            }
        children +=
            descriptor.enumTypeList.map {
                EnumDescriptor(this, it)
            }
        children +=
            descriptor.extensionList.map {
                ExtensionDescriptor(this, it)
            }
        children +=
            descriptor.oneofDeclList.map {
                OneofFieldDescriptor(this, it)
            }
        super.resolveChildren(children)
    }

    val fields: List<MessageFieldDescriptor> get() = children().filterIsInstance<MessageFieldDescriptor>()

    val oneofs: List<OneofFieldDescriptor> get() = children().filterIsInstance<OneofFieldDescriptor>()

    val messages: List<MessageDescriptor> get() = children().filterIsInstance<MessageDescriptor>()

    val enums: List<EnumDescriptor> get() = children().filterIsInstance<EnumDescriptor>()

    val extensions: List<ExtensionDescriptor> get() = children().filterIsInstance<ExtensionDescriptor>()

    fun fullProtoName(): String {
        return when (val parent = this.parent) {
            is MessageDescriptor -> "${parent.fullProtoName()}.${descriptor.name}"
            is FileDescriptor -> ".${parent.descriptor.`package`}.${descriptor.name}"
            else -> TODO()
        }
    }

    fun name(): String {
        return descriptor.name
    }

    fun mutableName(): String {
        return "Mutable${name()}"
    }

    fun implementationName(): String {
        return "${name()}Impl"
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

    fun mutableClassName(): ClassName {
        return when (val parent = this.parent) {
            is MessageDescriptor -> {
                parent.mutableClassName().nestedClass(mutableName())
            }
            is FileDescriptor -> {
                ClassName(parent.internalPackageName(), mutableName())
            }
            else -> TODO()
        }
    }

    fun implementationClassName(): ClassName {
        return when (val parent = this.parent) {
            is MessageDescriptor -> {
                parent.implementationClassName().nestedClass(implementationName())
            }
            is FileDescriptor -> {
                ClassName(parent.internalPackageName(), implementationName())
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

    fun path(): List<Int> {
        val path = mutableListOf<Int>()
        var state: Any? = this
        while (state is MessageDescriptor) {
            when (val parent = this.parent) {
                is MessageDescriptor -> {
                    path += parent.descriptor.nestedTypeList.indexOf(state.descriptor)
                    path += DescriptorProtos.DescriptorProto.NESTED_TYPE_FIELD_NUMBER
                }
                is FileDescriptor -> {
                    path += parent.descriptor.messageTypeList.indexOf(state.descriptor)
                    path += DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER
                }
            }
            state = state.parent
        }
        path.reverse()
        return path
    }

    fun document(): String {
        return escapeDoc(
            file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
                it.pathList.contentEquals(path())
            }?.leadingComments ?: "",
        )
    }

    fun mapEntry(): Boolean {
        return descriptor.options.mapEntry == true
    }
}
