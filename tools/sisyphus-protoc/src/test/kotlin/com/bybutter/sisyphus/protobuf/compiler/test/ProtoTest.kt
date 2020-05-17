package com.bybutter.sisyphus.protobuf.compiler.test

import com.bybutter.sisyphus.io.toUnixPath
import com.bybutter.sisyphus.protobuf.compiler.ProtobufGenerateContext
import com.bybutter.sisyphus.protobuf.compiler.ProtocRunner
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import org.junit.jupiter.api.Test

class ProtoTest {
    @Test
    fun `compile porto to kotlin`() {
        val source = mutableSetOf<String>()
        val protoPath = Paths.get(System.getProperty("user.dir"), "src/test/proto")
        Files.walkFileTree(protoPath, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.fileName.toString().endsWith(".proto")) {
                    source.add(protoPath.relativize(file).toString().toUnixPath())
                }
                return FileVisitResult.CONTINUE
            }
        })

        val context = ProtobufGenerateContext()

        val packageMapping = mapOf(
            "google.protobuf" to "com.bybutter.sisyphus.protobuf.primitives",
            "google.protobuf.compiler" to "com.bybutter.sisyphus.protobuf.compiler",
            "google.api" to "com.bybutter.sisyphus.api",
            "google.cloud.audit" to "com.bybutter.sisyphus.cloud.audit",
            "google.geo.type" to "com.bybutter.sisyphus.geo.type",
            "google.logging.type" to "com.bybutter.sisyphus.logging.type",
            "google.longrunning" to "com.bybutter.sisyphus.longrunning",
            "google.rpc" to "com.bybutter.sisyphus.rpc",
            "google.rpc.context" to "com.bybutter.sisyphus.rpc.context",
            "google.type" to "com.bybutter.sisyphus.type",
            "grpc.reflection.v1" to "com.bybutter.starter.grpc.support.reflection.v1",
            "grpc.reflection.v1alpha" to "com.bybutter.starter.grpc.support.reflection.v1alpha"
        )

        val result = context.generate(ProtocRunner.generate(protoPath.toFile(), source), source, packageMapping)
        for (compileResult in result) {
            compileResult.file.writeTo(Paths.get(System.getProperty("user.dir"), "src/test/kotlin/generated"))
            compileResult.implFile.writeTo(Paths.get(System.getProperty("user.dir"), "src/test/kotlin/generated"))
        }
    }
}
