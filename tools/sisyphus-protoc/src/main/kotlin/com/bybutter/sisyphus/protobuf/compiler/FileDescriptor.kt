package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import java.nio.file.Paths

class FileDescriptor(
    val parent: FileDescriptorSet,
    override val descriptor: DescriptorProtos.FileDescriptorProto
) : DescriptorNode<DescriptorProtos.FileDescriptorProto> {
    val messages: List<MessageDescriptor> = descriptor.messageTypeList.map {
        MessageDescriptor(this, it)
    }

    val enums: List<EnumDescriptor> = descriptor.enumTypeList.map {
        EnumDescriptor(this, it)
    }

    val extensions: List<ExtensionDescriptor> = descriptor.extensionList.map {
        ExtensionDescriptor(this, it)
    }

    val services: List<ServiceDescriptor> = descriptor.serviceList.map {
        ServiceDescriptor(this, it)
    }

    fun packageName(): String {
        val packageName = if (descriptor.options.hasJavaPackage()) {
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
