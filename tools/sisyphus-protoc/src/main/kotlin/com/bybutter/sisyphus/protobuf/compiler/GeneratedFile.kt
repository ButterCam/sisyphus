package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.io.replaceExtensionName
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Files
import java.nio.file.Path

interface GeneratedFile {
    fun writeTo(path: Path)
}

class GeneratedKotlinFile(val file: FileSpec) : GeneratedFile {
    override fun writeTo(path: Path) {
        file.writeTo(path)
    }
}

class GeneratedDescriptorFile(val descriptor: DescriptorProtos.FileDescriptorProto) : GeneratedFile {
    override fun writeTo(path: Path) {
        val descriptorFile = path.resolve(descriptor.name.replaceExtensionName("proto", "pb"))
        Files.createDirectories(descriptorFile.parent)
        Files.write(path.resolve(descriptorFile), descriptor.toByteArray())
    }
}
