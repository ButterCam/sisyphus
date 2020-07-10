package com.bybutter.sisyphus.apilinter

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Paths
import java.util.Locale
import org.springframework.core.io.ClassPathResource

class ApiLinterRunner {

    fun runApiLinter(args: List<String>, version: String): String? {
        val apiLinterTemp = extractApiLinter(version)
        return apiLinterTemp?.absolutePath?.let { executeCmd(it, args) }
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

    private fun extractApiLinter(version: String): File? {
        val tempBinDir = createTempBinDir()
        val filePath = Paths.get(version, fileName(version)).toString()
        val srcFilePath = if (ClassPathResource(filePath).exists()) {
            println("api-linter: api linter version is $version")
            filePath
        } else {
            println("api-linter: api linter version is $API_LINTER_DEFAULT_VERSION")
            Paths.get(API_LINTER_DEFAULT_VERSION, fileName(API_LINTER_DEFAULT_VERSION)).toString()
        }
        if (!ClassPathResource(srcFilePath).exists()) {
            println("api-linter: check your resource file, can't find api linter exe file, skip api linter")
            return null
        }
        val apiLinterTemp = File(tempBinDir, "apilinter.exe")
        populateFile(srcFilePath, apiLinterTemp)
        apiLinterTemp.setExecutable(true)
        apiLinterTemp.deleteOnExit()
        return apiLinterTemp
    }

    private fun populateFile(srcFilePath: String, destFile: File) {
        val inputStream = ClassPathResource(srcFilePath).inputStream
        val outputStream = FileOutputStream(destFile)
        try {
            streamCopy(inputStream, outputStream)
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }

    private fun streamCopy(inputStream: InputStream, outputStream: OutputStream) {
        var read = 0
        val buf = ByteArray(4096)
        while (inputStream.read(buf).also { read = it } > 0) outputStream.write(buf, 0, read)
    }

    private fun fileName(version: String): String {
        val osName = System.getProperties().getProperty("os.name").normalize()
        val platform = when {
            (osName.startsWith("macosx") || osName.startsWith("osx")) -> "darwin"
            osName.startsWith("linux") -> "linux"
            osName.startsWith("windows") -> "windows"
            else -> null
        }
        println("api-linter: detected platform is $platform , api linter version is $version")
        return "api-linter-$version-$platform.exe"
    }

    private fun String.normalize(): String {
        return this.toLowerCase(Locale.US).replace("[^a-z0-9]+".toRegex(), "")
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
