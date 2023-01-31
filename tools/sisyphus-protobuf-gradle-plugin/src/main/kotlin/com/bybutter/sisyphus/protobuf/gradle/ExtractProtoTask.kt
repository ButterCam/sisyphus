package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.io.toUnixPath
import com.bybutter.sisyphus.protobuf.compiler.ProtocRunner
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

open class ExtractProtoTask : DefaultTask() {
    @get:OutputDirectory
    val resourceOutput: DirectoryProperty = project.objects.directoryProperty()

    @get:OutputDirectory
    val protoPath: DirectoryProperty = project.objects.directoryProperty()

    @get:InputFiles
    val protoCompileFiles: ConfigurableFileCollection = project.objects.fileCollection()

    @get:InputFiles
    val protoApiFiles: ConfigurableFileCollection = project.objects.fileCollection()

    @get:Internal
    lateinit var protobuf: ProtobufExtension

    private val scannedMapping = mutableMapOf<String, String>()
    private val sourceProtos = mutableSetOf<String>()
    private val sourceFileMapping = mutableMapOf<String, String>()

    private fun addProto(file: File) {
        addProto(file, false)
    }

    private fun addSource(file: File) {
        addProto(file, true)
    }

    private fun addProto(file: File, source: Boolean) {
        if (file.isFile) {
            FileSystems.newFileSystem(file.toPath(), javaClass.classLoader).use {
                for (rootDirectory in it.rootDirectories) {
                    addProtoInternal(rootDirectory, source)
                }
            }
        } else {
            addProtoInternal(file.toPath(), source)
        }
    }

    private fun addProtoInternal(dir: Path, source: Boolean) {
        if (!Files.exists(dir)) {
            return
        }

        Files.walkFileTree(
            dir,
            object : SimpleFileVisitor<Path>() {
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
            }
        )
    }

    private fun addProtoInternal(name: String, value: ByteArray, file: Path, source: Boolean) {
        if (source) {
            val protoName = name.toUnixPath()
            sourceProtos += protoName
            sourceFileMapping[protoName] = file.toString()
        }
        val targetFile = protoPath.asFile.get().toPath().resolve(name)
        Files.createDirectories(targetFile.parent)
        Files.deleteIfExists(targetFile)
        Files.write(targetFile, value)
    }

    @TaskAction
    fun extractProto() {
        resourceOutput.asFile.get().deleteRecursively()
        resourceOutput.asFile.get().mkdirs()

        scannedMapping += protobuf.mapping

        for (file in protoApiFiles) {
            addProto(file)
        }

        for (file in protoCompileFiles) {
            addSource(file)
        }

        if (protobuf.mapping.isNotEmpty()) {
            Files.write(
                Paths.get(resourceOutput.asFile.get().toPath().toString(), "protomap"),
                protobuf.mapping.map { "${it.key}=${it.value}" }
            )
        }

        val desc = ProtocRunner.generate(protoPath.asFile.get(), sourceProtos)
        Files.write(Paths.get(protoPath.asFile.get().toPath().toString(), "protodesc.pb"), desc.toByteArray())
        Files.write(
            Paths.get(protoPath.asFile.get().toPath().toString(), "protomap"),
            scannedMapping.map { "${it.key}=${it.value}" }
        )
        Files.write(Paths.get(protoPath.asFile.get().toPath().toString(), "protosrc"), sourceProtos)
        Files.write(
            Paths.get(protoPath.asFile.get().toPath().toString(), "protofile"),
            sourceFileMapping.map { "${it.key}=${it.value}" }
        )
    }
}
