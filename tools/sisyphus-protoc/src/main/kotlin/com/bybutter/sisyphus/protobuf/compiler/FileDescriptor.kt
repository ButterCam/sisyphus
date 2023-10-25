package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import java.nio.file.Paths

class FileDescriptor(
    override val parent: FileDescriptorSet,
    override val descriptor: DescriptorProtos.FileDescriptorProto,
) : DescriptorNode<DescriptorProtos.FileDescriptorProto>() {
    override fun resolveChildren(children: MutableList<DescriptorNode<*>>) {
        children +=
            descriptor.messageTypeList.map {
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
            descriptor.serviceList.map {
                ServiceDescriptor(this, it)
            }
        super.resolveChildren(children)
    }

    val messages: List<MessageDescriptor> get() = children().filterIsInstance<MessageDescriptor>()

    val enums: List<EnumDescriptor> get() = children().filterIsInstance<EnumDescriptor>()

    val extensions: List<ExtensionDescriptor> get() = children().filterIsInstance<ExtensionDescriptor>()

    val services: List<ServiceDescriptor> get() = children().filterIsInstance<ServiceDescriptor>()

    fun packageName(): String {
        val packageName =
            if (descriptor.options.hasJavaPackage()) {
                descriptor.options.javaPackage
            } else {
                descriptor.`package`
            }

        return parent.shadePackage(packageName)
    }

    fun internalPackageName(): String {
        return "${packageName()}.internal"
    }

    fun kotlinFileName(): String {
        return Paths.get(descriptor.name).toFile().nameWithoutExtension.toPascalCase()
    }

    fun fileMetadataName(): String {
        return "${kotlinFileName()}Metadata"
    }

    fun fileMetadataClassName(): ClassName {
        return ClassName.bestGuess("${internalPackageName()}.${fileMetadataName()}".trim('.'))
    }
}
