package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.compiler.core.state.FileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec

class ProtobufCompiler(
    files: DescriptorProtos.FileDescriptorSet,
    packageShading: Map<String, String> = mapOf(),
    val generators: com.bybutter.sisyphus.protobuf.compiler.CodeGenerators = com.bybutter.sisyphus.protobuf.compiler.CodeGenerators()
) {
    val descriptorSet = FileDescriptorSet(files, packageShading)

    fun generate(file: String): ProtoCompileResult {
        val fileDescriptor = descriptorSet.children.firstOrNull { it.descriptor.name == file }
            ?: throw IllegalArgumentException("Proto file '$file' not imported.")
        val result = mutableListOf<FileSpec>()
        FileGeneratingState(this, fileDescriptor, result).advance()
        return ProtoCompileResult(fileDescriptor, result)
    }
}
