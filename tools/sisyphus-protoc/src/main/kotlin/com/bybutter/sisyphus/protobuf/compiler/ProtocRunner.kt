package com.bybutter.sisyphus.protobuf.compiler

import com.github.os72.protocjar.Protoc
import com.google.protobuf.DescriptorProtos
import java.io.File
import java.nio.file.Files

open class ProtocRunner(val version: String = "3.11.4") {
    companion object : ProtocRunner()

    fun generate(protoPath: File, source: Collection<String>): DescriptorProtos.FileDescriptorSet {
        if (source.isEmpty()) {
            return DescriptorProtos.FileDescriptorSet.newBuilder().build()
        }

        val outputFile = Files.createTempFile("out", ".pb")
        val result = Protoc.runProtoc(
            arrayOf(
                "-v$version",
                "-o$outputFile",
                "-I$protoPath",
                "--include_imports",
                "--include_source_info",
                *source.toTypedArray()
            ),
            System.out, System.out
        )
        if (result != 0) {
            throw IllegalStateException("Protoc return '$result' with not zero value.")
        }

        val bytes = outputFile.toFile().readBytes()
        return DescriptorProtos.FileDescriptorSet.parseFrom(bytes)
    }
}
