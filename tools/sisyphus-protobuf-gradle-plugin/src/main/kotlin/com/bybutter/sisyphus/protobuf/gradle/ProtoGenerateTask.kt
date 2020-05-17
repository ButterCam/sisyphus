package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.protobuf.ProtoFileMeta
import com.bybutter.sisyphus.protobuf.compiler.ProtobufGenerateContext
import com.google.protobuf.DescriptorProtos
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

open class ProtoGenerateTask : SourceTask() {
    @get:InputDirectory
    @get:SkipWhenEmpty
    lateinit var protoPath: File

    @get:OutputDirectory
    lateinit var output: File

    @get:OutputDirectory
    lateinit var implOutput: File

    @get:OutputDirectory
    lateinit var resourceOutput: File

    private val protobufGenerateContext = ProtobufGenerateContext()

    @TaskAction
    fun generateKotlin() {
        val descFile = protoPath.resolve("protodesc.pb")
        val desc = if (descFile.exists()) {
            DescriptorProtos.FileDescriptorSet.parseFrom(descFile.readBytes())
        } else {
            return
        }

        val mappingFile = protoPath.resolve("protomap")
        val mapping = if (mappingFile.exists()) {
            mappingFile.readLines().mapNotNull {
                val map = it.split('=')
                if (map.size == 2) {
                    map[0] to map[1]
                } else {
                    null
                }
            }.associate { it }
        } else {
            mapOf()
        }

        val sourceFile = protoPath.resolve("protosrc")
        val source = if (sourceFile.exists()) {
            sourceFile.readLines().toSet()
        } else {
            setOf()
        }

        val result = protobufGenerateContext.generate(desc, source, mapping)

        for (generateResult in result) {
            generateResult.file.writeTo(output.toPath())
            generateResult.implFile.writeTo(implOutput.toPath())
        }

        val fileMetas = Paths.get(resourceOutput.toPath().toString(), "META-INF/services/${ProtoFileMeta::class.java.name}")
        Files.createDirectories(fileMetas.parent)
        Files.write(fileMetas, result.map { it.fileMeta })
    }
}
