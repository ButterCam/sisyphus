package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protoc.Protoc
import com.google.protobuf.DescriptorProtos
import java.io.File
import java.nio.file.Files

object ProtocRunner {
    fun generate(protoPath: File, source: Collection<String>): DescriptorProtos.FileDescriptorSet {
        if (source.isEmpty()) {
            return DescriptorProtos.FileDescriptorSet.newBuilder().build()
        }

        val outputFile = Files.createTempFile("out", ".pb")
        val arguments = arrayOf(
            "-o$outputFile",
            "-I$protoPath",
            "--include_imports",
            "--include_source_info",
            *source.toTypedArray()
        )
        Protoc.runProtoc(arguments)

        val bytes = outputFile.toFile().readBytes()
        return DescriptorProtos.FileDescriptorSet.parseFrom(bytes)
    }
}
