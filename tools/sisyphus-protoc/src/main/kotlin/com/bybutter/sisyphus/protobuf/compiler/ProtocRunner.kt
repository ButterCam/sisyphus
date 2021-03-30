package com.bybutter.sisyphus.protobuf.compiler

import com.github.os72.protocjar.Protoc
import com.github.os72.protocjar.ProtocVersion
import com.google.protobuf.DescriptorProtos
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files

object ProtocRunner {
    fun generate(protoPath: File, source: Collection<String>): DescriptorProtos.FileDescriptorSet {
        if (source.isEmpty()) {
            return DescriptorProtos.FileDescriptorSet.newBuilder().build()
        }

        val outputFile = Files.createTempFile("out", ".pb")
        val arguments = arrayOf(
            "-v${ProtocVersion.PROTOC_VERSION.mVersion}",
            "-o$outputFile",
            "-I$protoPath",
            "--include_imports",
            "--include_source_info",
            *source.toTypedArray()
        )

        val result = try {
            Protoc.extractProtoc(ProtocVersion.PROTOC_VERSION, false)
            Protoc.runProtoc(arguments, System.out, System.out)
        } catch (e: FileNotFoundException) {
            Protoc.runProtoc("protoc", arguments.toList(), System.out, System.out)
        }

        if (result != 0) {
            throw IllegalStateException("Protoc return '$result' with not zero value.")
        }

        val bytes = outputFile.toFile().readBytes()
        return DescriptorProtos.FileDescriptorSet.parseFrom(bytes)
    }
}
