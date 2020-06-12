package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec

class ProtobufGenerateContext : ProtobufElement() {
    override val parent: ProtobufElement get() = this
    override val kotlinName: String = ""
    override val protoName: String = ""

    var packageMapping = mapOf<String, String>()

    val resourceNames = mutableMapOf<String, ResourceNameParentGenerator>()

    fun generate(descriptorProto: DescriptorProtos.FileDescriptorSet, source: Set<String>, mapping: Map<String, String> = mapOf()): List<GeneratingResult> {
        clear()
        packageMapping = mapping
        if (source.isEmpty()) {
            return listOf()
        }

        for (fileDescriptorProto in descriptorProto.fileList) {
            addElement(FileGenerator(this, fileDescriptorProto))
        }
        prepareGenerating()
        return children.mapNotNull { it as? FileGenerator }.filter {
            source.contains(it.protoFilePath)
        }.map {
            GeneratingResult(it.generate(), it.generateInternal(), FileDescriptorProto.parse(it.descriptor.toByteArray()), it.fileMetaTypeName.toString())
        }
    }
}

data class GeneratingResult(val file: FileSpec, val implFile: FileSpec, val descriptor: FileDescriptorProto, val fileMeta: String)
