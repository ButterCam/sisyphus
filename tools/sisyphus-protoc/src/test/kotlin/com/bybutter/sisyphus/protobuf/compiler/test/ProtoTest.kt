package com.bybutter.sisyphus.protobuf.compiler.test

import com.bybutter.sisyphus.io.toUnixPath
import com.bybutter.sisyphus.protobuf.compiler.CodeGenerators
import com.bybutter.sisyphus.protobuf.compiler.ProtobufCompiler
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
        val protoPath = Paths.get(System.getProperty("user.dir"), "src/test/resources")
        Files.walkFileTree(protoPath, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.fileName.toString().endsWith(".proto")) {
                    source.add(protoPath.relativize(file).toString().toUnixPath())
                }
                return FileVisitResult.CONTINUE
            }
        })

        val desc = ProtocRunner.generate(protoPath.toFile(), source)
        val compiler = ProtobufCompiler(desc, mapOf(
            "com.google.protobuf" to "com.bybutter.sisyphus.protobuf.primitives",
            "com.google.protobuf.compiler" to "com.bybutter.sisyphus.protobuf.compiler",
            "com.google.api" to "com.bybutter.sisyphus.api",
            "com.google.cloud.audit" to "com.bybutter.sisyphus.cloud.audit",
            "com.google.geo.type" to "com.bybutter.sisyphus.geo.type",
            "com.google.logging.type" to "com.bybutter.sisyphus.logging.type",
            "com.google.longrunning" to "com.bybutter.sisyphus.longrunning",
            "com.google.rpc" to "com.bybutter.sisyphus.rpc",
            "com.google.rpc.context" to "com.bybutter.sisyphus.rpc.context",
            "com.google.type" to "com.bybutter.sisyphus.type",
            "io.grpc.reflection.v1" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1",
            "io.grpc.reflection.v1alpha" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha"
        ), CodeGenerators().basic().resourceName().coroutineService())
        for (s in source) {
            val result = compiler.generate(s)
            for (file in result.files) {
                file.writeTo(Paths.get(System.getProperty("user.dir"), "src/test/java"))
            }
        }
    }
}
