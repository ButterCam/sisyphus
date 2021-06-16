package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.protobuf.compiler.CodeGenerators
import com.bybutter.sisyphus.protobuf.compiler.GeneratedDescriptorFile
import com.bybutter.sisyphus.protobuf.compiler.GeneratedKotlinFile
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

open class GenerateProtoTask : DefaultTask() {
    @get:InputDirectory
    lateinit var protoPath: File

    @get:OutputDirectory
    lateinit var output: File

    @get:OutputDirectory
    lateinit var resourceOutput: File

    @get:Internal
    lateinit var protobuf: ProtobufExtension

    @TaskAction
    fun generateKotlin() {
        output.deleteRecursively()
        output.mkdirs()

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

        val compiler = ProtobufCompiler(desc, mapping, protobuf.plugins.toCodeGenerators())
        val results = compiler.generate(source)
        for (result in results.results) {
            val sourceProto = result.descriptor.descriptor.name
            if (protobuf.source) {
                val sourceProtoFile = protoPath.resolve(sourceProto).toPath()
                val protoFile = Paths.get(resourceOutput.toPath().toString(), sourceProto)
                Files.createDirectories(protoFile.parent)
                Files.copy(sourceProtoFile, protoFile, StandardCopyOption.REPLACE_EXISTING)
            }
            for (file in result.files) {
                when (file) {
                    is GeneratedKotlinFile -> {
                        file.writeTo(output.toPath())
                    }
                    is GeneratedDescriptorFile -> {
                        file.writeTo(resourceOutput.toPath())
                    }
                }
            }
        }
        results.booster?.let {
            it.writeTo(output.toPath())
            val booster = Paths.get(
                resourceOutput.toPath().toString(),
                "META-INF/services/${RuntimeTypes.PROTOBUF_BOOSTER.canonicalName}"
            )
            val boosterName = "${it.packageName}.${(it.members.first() as TypeSpec).name}"
            Files.createDirectories(booster.parent)
            Files.write(booster, boosterName.toByteArray(Charset.defaultCharset()))
        }
    }

    companion object {
        private fun ProtoCompilerPlugins.toCodeGenerators(): CodeGenerators {
            return CodeGenerators().apply {
                for (buildInPlugin in this@toCodeGenerators.buildInPlugins) {
                    when (buildInPlugin) {
                        BuildInPlugin.BASIC_GENERATOR -> basic()
                        BuildInPlugin.COROUTINE_SERVICE_GENERATOR -> coroutineService()
                        BuildInPlugin.SEPARATED_COROUTINE_SERVICE_GENERATOR -> separatedCoroutineService()
                        BuildInPlugin.RXJAVA_SERVICE_GENERATOR -> rxjavaClient()
                        BuildInPlugin.SEPARATED_RXJAVA_SERVICE_GENERATOR -> separatedRxjavaClient()
                        BuildInPlugin.RESOURCE_NAME_GENERATOR -> resourceName()
                        BuildInPlugin.GENERATORS_FROM_SPI -> spi()
                        BuildInPlugin.LITE_DESCRIPTOR_GENERATOR -> liteDescriptor()
                        BuildInPlugin.INLINE_DESCRIPTOR_GENERATOR -> inlineDescriptor()
                    }
                }
                register(this@toCodeGenerators.plugins)
            }
        }
    }
}
