package com.bybutter.sisyphus.protoc

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

object Protoc {
    private const val DEFAULT_PROTOC_VERSION = "3.17.3"

    fun runProtoc(args: Array<String>) {
        val cmd = try {
            extractProtoc(DEFAULT_PROTOC_VERSION)
        } catch (unsupportedException: UnsupportedOperationException) {
            val localVersion = try {
                ProcessBuilder(arrayListOf("protoc", "--version")).redirectErrorStream(true).start().text()
            } catch (e: IOException) {
                throw UnsupportedOperationException("${unsupportedException.message}, and local protoc can not be found.")
            }
            println("Due to ${unsupportedException.message}, version $localVersion is used.")
            "protoc"
        }
        runProtoc(cmd, args)
    }

    fun runProtoc(cmd: String, args: Array<String>) {
        executeCmd(cmd, args)
    }

    private fun executeCmd(cmd: String, args: Array<String>) {
        val protocCmd = arrayListOf(cmd, *args)
        println("protoc executing: $protocCmd")
        val process = ProcessBuilder(protocCmd).redirectErrorStream(true).start()
        val result = process.text()
        println("protoc: $result")
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            println("protoc: $result")
            throw IllegalStateException("Protoc runner return with non-zero value '$exitCode'.")
        }
    }

    private fun extractProtoc(version: String): String {
        val platform = PlatformDetector.detect()
        println("detected platform: $platform")
        val srcFilePath =
            Paths.get(version, "protoc-$version-${platform.osClassifier}.exe").toString()
        val srcFile = this.javaClass.classLoader.getResource(srcFilePath.replace(File.separatorChar, '/'))
            ?: throw UnsupportedOperationException("Unsupported protoc version $version for platform ${platform.osClassifier}")
        val executable = createTempBinDir().resolve("protoc.exe")
        srcFile.openStream().use {
            Files.copy(it, executable)
        }
        if (executable.fileSystem.supportedFileAttributeViews().contains("posix")) {
            Files.setPosixFilePermissions(executable, setOf(PosixFilePermission.OWNER_EXECUTE))
        }
        return executable.also {
            it.toFile().deleteOnExit()
        }.toString()
    }

    private fun createTempBinDir(): Path {
        return Files.createTempDirectory("protoc-runner").also {
            it.toFile().deleteOnExit()
        }
    }

    private fun Process.text(): String {
        return this.inputStream.bufferedReader().use(BufferedReader::readText)
    }
}
