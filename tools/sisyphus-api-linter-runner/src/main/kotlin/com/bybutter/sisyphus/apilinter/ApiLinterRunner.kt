package com.bybutter.sisyphus.apilinter

import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

class ApiLinterRunner {

    fun runApiLinter(args: List<String>, version: String): String? {
        val apiLinterTemp = extractApiLinter(version)
        return executeCmd(apiLinterTemp.toString(), args)
    }

    private fun executeCmd(cmd: String, args: List<String>): String {
        val apiLinterCmd = mutableListOf(cmd)
        for (arg in args) {
            apiLinterCmd.add(arg)
        }
        println("api-linter executing: $apiLinterCmd")
        val process = ProcessBuilder(apiLinterCmd).redirectErrorStream(true).start()
        val result = process.text()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            println("api-linter: $result")
            throw IllegalStateException("Api linter return with non-zero value '$exitCode'.")
        }
        return result
    }

    private fun extractApiLinter(version: String): Path {
        val osName = System.getProperties().getProperty("os.name").normalize()
        val platform = detectPlatform(osName)
        val srcFilePath = Paths.get(version, "api-linter-$version-$platform.exe").toString()
        val srcFile = this.javaClass.classLoader.getResource(srcFilePath.replace(File.separatorChar, '/'))
            ?: throw UnsupportedOperationException("Unsupported api linter version $version for platform $osName.")
        val executable = createTempBinDir().resolve("apilinter.exe")
        srcFile.openStream().use {
            Files.copy(it, executable)
        }
        when (platform) {
            "darwin", "linux" -> {
                Files.setPosixFilePermissions(executable, setOf(PosixFilePermission.OWNER_EXECUTE))
            }
        }
        return executable.also {
            it.toFile().deleteOnExit()
        }
    }

    private fun detectPlatform(osName: String): String {
        return when {
            (osName.startsWith("macosx") || osName.startsWith("osx")) -> "darwin"
            osName.startsWith("linux") -> "linux"
            osName.startsWith("windows") -> "windows"
            else -> "unknown"
        }
    }

    private fun String.normalize(): String {
        return this.toLowerCase().replace("[^a-z0-9]+".toRegex(), "")
    }

    private fun createTempBinDir(): Path {
        return Files.createTempDirectory("apilinterrun").also {
            it.toFile().deleteOnExit()
        }
    }

    private fun Process.text(): String {
        return this.inputStream.bufferedReader().use(BufferedReader::readText)
    }

    companion object {
        const val API_LINTER_DEFAULT_VERSION = "1.1.0"
    }
}
