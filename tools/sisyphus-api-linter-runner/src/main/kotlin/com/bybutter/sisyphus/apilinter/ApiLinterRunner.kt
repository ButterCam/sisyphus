package com.bybutter.sisyphus.apilinter

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import org.springframework.core.io.ClassPathResource

class ApiLinterRunner {

    fun runApiLinter(args: List<String>, version: String): String? {
        val apiLinterTemp = extractApiLinter(version)
        return executeCmd(apiLinterTemp.absolutePath, args)
    }

    private fun executeCmd(cmd: String, args: List<String>): String {
        val apiLinterCmd = mutableListOf(cmd)
        for (arg in args) {
            apiLinterCmd.add(arg)
        }
        println("api-linter executing: $apiLinterCmd")
        val process = ProcessBuilder(apiLinterCmd).start()
        val result = process.text()
        process.waitFor()
        return result
    }

    private fun extractApiLinter(version: String): File {
        val tempBinDir = createTempBinDir()
        val osName = System.getProperties().getProperty("os.name").normalize()
        val platform = detectPlatform(osName)
        val srcFilePath = Paths.get(version, "api-linter-$version-$platform.exe").toString()
        val srcFile = ClassPathResource(srcFilePath)
        if (!srcFile.exists()) {
            throw UnsupportedOperationException("Unsupported api linter version $version or platform $osName.")
        }
        val apiLinterTemp = File(tempBinDir, "apilinter.exe")
        populateFile(srcFile, apiLinterTemp)
        apiLinterTemp.setExecutable(true)
        apiLinterTemp.deleteOnExit()
        return apiLinterTemp
    }

    private fun detectPlatform(osName: String): String {
        return when {
            (osName.startsWith("macosx") || osName.startsWith("osx")) -> "darwin"
            osName.startsWith("linux") -> "linux"
            osName.startsWith("windows") -> "windows"
            else -> "unknown"
        }
    }

    private fun populateFile(srcFile: ClassPathResource, destFile: File) {
        val inputStream = srcFile.inputStream
        val outputStream = FileOutputStream(destFile)
        try {
            inputStream.copyTo(outputStream)
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }

    private fun String.normalize(): String {
        return this.toLowerCase().replace("[^a-z0-9]+".toRegex(), "")
    }

    private fun createTempBinDir(): File {
        val tempDir = File.createTempFile("apilinterrun", "")
        tempDir.delete()
        tempDir.mkdir()
        tempDir.deleteOnExit()
        val binDir = File(tempDir, "bin")
        binDir.mkdir()
        binDir.deleteOnExit()
        return binDir
    }

    private fun Process.text(): String {
        return this.inputStream.bufferedReader().use(BufferedReader::readText)
    }

    companion object {
        const val API_LINTER_DEFAULT_VERSION = "1.1.0"
    }
}
