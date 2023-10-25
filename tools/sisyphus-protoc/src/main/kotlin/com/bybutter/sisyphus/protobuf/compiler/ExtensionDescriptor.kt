package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member

class ExtensionDescriptor(
    override val parent: DescriptorNode<*>,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
) : DescriptorNode<DescriptorProtos.FieldDescriptorProto>() {
    fun supportName(): String {
        return "${this.descriptor.jsonName.toPascalCase()}ExtensionSupportFor${extendee().descriptor.name}"
    }

    fun supportClassName(): ClassName {
        return when (val parent = parent) {
            is FileDescriptor -> ClassName(parent.internalPackageName(), supportName())
            is MessageDescriptor -> parent.supportClassName().nestedClass(supportName())
            else -> TODO()
        }
    }

    fun propertyMemberName(): MemberName {
        return when (val parent = parent) {
            is FileDescriptor -> MemberName(parent.packageName(), descriptor.jsonName)
            is MessageDescriptor -> parent.className().member(descriptor.jsonName)
            else -> TODO()
        }
    }

    fun extendee(): MessageDescriptor {
        return fileSet().ensureMessage(descriptor.extendee)
    }

    fun document(): String {
        return escapeDoc(
            file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
                it.pathList.contentEquals(path())
            }?.leadingComments ?: "",
        )
    }

    fun path(): List<Int> {
        return when (val parent = parent) {
            is FileDescriptor ->
                listOf<Int>() + DescriptorProtos.FileDescriptorProto.EXTENSION_FIELD_NUMBER +
                    parent.descriptor.extensionList.indexOf(
                        descriptor,
                    )
            is MessageDescriptor ->
                parent.path() + DescriptorProtos.DescriptorProto.EXTENSION_FIELD_NUMBER +
                    parent.descriptor.extensionList.indexOf(
                        descriptor,
                    )
            else -> TODO()
        }
    }
}
