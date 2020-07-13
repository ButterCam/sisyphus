package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.io.toUnixPath
import com.bybutter.sisyphus.protobuf.compiler.ProtocRunner
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

open class ExtractProtoTask : SourceTask() {
    @get:InputFiles
    lateinit var input: FileCollection

    @get:OutputDirectory
    lateinit var resourceOutput: File

    @get:OutputDirectory
    lateinit var protoPath: File

    @get:Internal
    lateinit var protoConfig: Configuration

    @get:Internal
    lateinit var protoApiConfig: Configuration

    @get:Internal
    lateinit var protobuf: ProtobufExtension

    private val scannedMapping = mutableMapOf<String, String>()
    private val sourceProtos = mutableSetOf<String>()
    private val sourceFileMapping = mutableMapOf<String, String>()

    private fun addProto(file: File) {
        addProto(file.toPath())
    }

    private fun addProto(path: Path) {
        val file = path.toFile()
        if (file.isFile) {
            FileSystems.newFileSystem(path, javaClass.classLoader).use {
                for (rootDirectory in it.rootDirectories) {
                    addProtoInternal(rootDirectory, false)
                }
            }
        } else {
            addProtoInternal(path, false)
        }
    }

    private fun addProtoInternal(dir: Path, source: Boolean) {
        if (!Files.exists(dir)) {
            return
        }

        Files.walkFileTree(dir, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.fileName.toString().endsWith(".proto")) {
                    addProtoInternal(dir.relativize(file).toString(), Files.readAllBytes(file), file, source)
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

    private fun addProtoInternal(name: String, value: ByteArray, file: Path, source: Boolean) {
        if (source) {
            val protoName = name.toUnixPath()
            sourceProtos.add(protoName)
            sourceFileMapping[protoName] = URI(file.toUri().toURL().path).path
        }
        val targetFile = protoPath.toPath().resolve(name)
        Files.createDirectories(targetFile.parent)
        Files.deleteIfExists(targetFile)
        Files.write(targetFile, value)
    }

    private fun addSource(file: File) {
        addSource(file.toPath())
    }

    private fun addSource(path: Path) {
        val file = path.toFile()
        if (file.isFile) {
            FileSystems.newFileSystem(path, javaClass.classLoader).use {
                for (rootDirectory in it.rootDirectories) {
                    addProtoInternal(rootDirectory, true)
                }
            }
        } else {
            addProtoInternal(path, true)
        }
    }

    @TaskAction
    fun extractProto() {
        scannedMapping += protobuf.mapping

        for (file in protoApiConfig.files) {
            addProto(file)
        }

        for (file in protoConfig.files) {
            addSource(file)
        }

        for (file in input) {
            addSource(file)
        }

        if (protobuf.mapping.isNotEmpty()) {
            Files.write(Paths.get(resourceOutput.toPath().toString(), "protomap"), protobuf.mapping.map { "${it.key}=${it.value}" })
        }

        val desc = ProtocRunner.generate(protoPath, sourceProtos)
        Files.write(Paths.get(protoPath.toPath().toString(), "protodesc.pb"), desc.toByteArray())
        Files.write(Paths.get(protoPath.toPath().toString(), "protomap"), scannedMapping.map { "${it.key}=${it.value}" })
        Files.write(Paths.get(protoPath.toPath().toString(), "protosrc"), sourceProtos)
        Files.write(Paths.get(protoPath.toPath().toString(), "protofile"), sourceFileMapping.map { "${it.key}=${it.value}" })

        val enableServices = protobuf.service?.apis?.map { it.name }?.toSet()
        val releaseProtos = mutableSetOf<String>()
        for (file in desc.fileList) {
            for (service in file.serviceList) {
                val serviceName = file.`package` + service.name

                if (enableServices == null || enableServices.contains(serviceName)) {
                    releaseProtos += file.name
                }
            }
        }
        val releaseProtoFiles = ProtocRunner.generate(protoPath, releaseProtos)
        Files.write(Paths.get(protoPath.toPath().toString(), "protorelease"), releaseProtoFiles.fileList.map { it.name })
    }
}
