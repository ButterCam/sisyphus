package com.bybutter.sisyphus.protobuf.compiler.test

import com.bybutter.sisyphus.io.toUnixPath
import com.bybutter.sisyphus.protobuf.compiler.ProtobufCompiler
import com.bybutter.sisyphus.protobuf.compiler.ProtocRunner
import com.bybutter.sisyphus.protobuf.compiler.CodeGenerators
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
            "com.google.protobuf.compiler" to "com.bybutter.sisyphus.protobuf.compiler"
        ), CodeGenerators().coroutineService())
        for (s in source) {
            val result = compiler.generate(s)
            for (file in result.files) {
                file.writeTo(Paths.get(System.getProperty("user.dir"), "src/test/java"))
            }
        }
    }
}
