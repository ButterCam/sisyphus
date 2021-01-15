package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.io.replaceExtensionName
import com.bybutter.sisyphus.protobuf.compiler.CodeGenerators
import com.bybutter.sisyphus.protobuf.compiler.ProtobufCompiler
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

open class ProtoGenerateTask : DefaultTask() {
    @get:InputDirectory
    lateinit var protoPath: File

    @get:OutputDirectory
    lateinit var output: File

    @get:OutputDirectory
    lateinit var implOutput: File

    @get:OutputDirectory
    lateinit var resourceOutput: File

    @get:Internal
    lateinit var protobuf: ProtobufExtension

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
            sourceFile.readLines().mapNotNull {
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

        val compiler = ProtobufCompiler(desc, mapping, protobuf.plugins.toCodeGenerators())
        val fileSupports = StringBuilder()

        for ((sourceProto, external) in source) {
            val result = compiler.generate(sourceProto)
            result.files[0].writeTo(output.toPath())
            result.files[1].writeTo(implOutput.toPath())

            val descPbFile = Paths.get(
                resourceOutput.toPath().toString(),
                result.descriptor.descriptor.name.replaceExtensionName("proto", "pb")
            )
            Files.createDirectories(descPbFile.parent)
            Files.write(descPbFile, result.descriptor.descriptor.toByteArray())

            if (external == "external") {
                val protoFile = Paths.get(resourceOutput.toPath().toString(), result.descriptor.descriptor.name)
                val sourceProtoFile = protoPath.resolve(sourceProto).toPath()
                Files.createDirectories(sourceProtoFile.parent)
                Files.copy(sourceProtoFile, protoFile, StandardCopyOption.REPLACE_EXISTING)
            }

            val fileSupport = result.files[1].members.first {
                val spec = it as? TypeSpec ?: return@first false
                spec.superclass == RuntimeTypes.FILE_SUPPORT
            } as TypeSpec
            fileSupports.appendln("${result.files[1].packageName}.${fileSupport.name}")
        }

        val fileMetas = Paths.get(
            resourceOutput.toPath().toString(),
            "META-INF/services/${RuntimeTypes.FILE_SUPPORT.canonicalName}"
        )
        Files.createDirectories(fileMetas.parent)
        Files.write(fileMetas, fileSupports.toString().toByteArray(Charset.defaultCharset()))
    }

    companion object {
        private fun ProtoCompilerPlugins.toCodeGenerators(): CodeGenerators {
            return CodeGenerators().apply {
                for (buildInPlugin in this@toCodeGenerators.buildInPlugins) {
                    when (buildInPlugin) {
                        BuildInPlugin.BASIC_GENERATOR -> basic()
                        BuildInPlugin.COROUTINE_SERVICE_GENERATOR -> coroutineService()
                        BuildInPlugin.RXJAVA_SERVICE_GENERATOR -> TODO()
                        BuildInPlugin.RESOURCE_NAME_GENERATOR -> resourceName()
                        BuildInPlugin.GENERATORS_FROM_SPI -> spi()
                    }
                }
                register(this@toCodeGenerators.plugins)
            }
        }

    }
}