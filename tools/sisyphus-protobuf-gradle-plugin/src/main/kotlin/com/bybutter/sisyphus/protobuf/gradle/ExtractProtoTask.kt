package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.io.toUnixPath
import com.bybutter.sisyphus.protobuf.compiler.ProtocRunner
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

open class ExtractProtoTask : DefaultTask() {
    @get:InputFiles
    lateinit var input: FileCollection

    @get:OutputDirectory
    lateinit var resourceOutput: File

    @get:OutputDirectory
    lateinit var protoPath: File

    @get:InputFiles
    lateinit var protoConfig: Configuration

    @get:InputFiles
    lateinit var protoApiConfig: Configuration

    @get:Internal
    lateinit var protobuf: ProtobufExtension

    private val scannedMapping = mutableMapOf<String, String>()
    private val sourceProtos = mutableMapOf<String, String>()
    private val sourceFileMapping = mutableMapOf<String, String>()

    private fun addProto(file: File) {
        addProto(file, ProtoFileType.IMPORT)
    }

    private fun addSource(file: File) {
        addProto(file, ProtoFileType.SOURCE)
    }

    private fun addExternalSource(file: File) {
        addProto(file, ProtoFileType.EXTERNAL_SOURCE)
    }

    private fun addProto(file: File, type: ProtoFileType) {
        if (file.isFile) {
            FileSystems.newFileSystem(file.toPath(), javaClass.classLoader).use {
                for (rootDirectory in it.rootDirectories) {
                    addProtoInternal(rootDirectory, type)
                }
            }
        } else {
            addProtoInternal(file.toPath(), type)
        }
    }

    private fun addProtoInternal(dir: Path, type: ProtoFileType) {
        if (!Files.exists(dir)) {
            return
        }

        Files.walkFileTree(dir, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.fileName.toString().endsWith(".proto")) {
                    addProtoInternal(dir.relativize(file).toString(), Files.readAllBytes(file), file, type)
                }
                if (file.endsWith("protomap")) {
                    scannedMapping += Files.readAllLines(file).mapNotNull {
                        val map = it.split('=')
                        if (map.size == 2) {
                            map[0] to map[1]
                        } else {
                            null
                        }
                    }
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun addProtoInternal(name: String, value: ByteArray, file: Path, type: ProtoFileType) {
        if (type.source) {
            val protoName = name.toUnixPath()
            sourceProtos[protoName] = if (type == ProtoFileType.EXTERNAL_SOURCE) "external" else "source"
            sourceFileMapping[protoName] = file.toString()
        }
        val targetFile = protoPath.toPath().resolve(name)
        Files.createDirectories(targetFile.parent)
        Files.deleteIfExists(targetFile)
        Files.write(targetFile, value)
    }

    @TaskAction
    fun extractProto() {
        scannedMapping += protobuf.mapping

        for (file in protoApiConfig.files) {
            addProto(file)
        }

        for (file in protoConfig.files) {
            addExternalSource(file)
        }

        for (file in input) {
            addSource(file)
        }

        if (protobuf.mapping.isNotEmpty()) {
            Files.write(Paths.get(resourceOutput.toPath().toString(), "protomap"), protobuf.mapping.map { "${it.key}=${it.value}" })
        }

        val desc = ProtocRunner.generate(protoPath, sourceProtos.keys)
        Files.write(Paths.get(protoPath.toPath().toString(), "protodesc.pb"), desc.toByteArray())
        Files.write(Paths.get(protoPath.toPath().toString(), "protomap"), scannedMapping.map { "${it.key}=${it.value}" })
        Files.write(Paths.get(protoPath.toPath().toString(), "protosrc"), sourceProtos.map { "${it.key}=${it.value}" })
        Files.write(Paths.get(protoPath.toPath().toString(), "protofile"), sourceFileMapping.map { "${it.key}=${it.value}" })
    }
}

enum class ProtoFileType(val source: Boolean) {
    IMPORT(false), SOURCE(true), EXTERNAL_SOURCE(true)
}
