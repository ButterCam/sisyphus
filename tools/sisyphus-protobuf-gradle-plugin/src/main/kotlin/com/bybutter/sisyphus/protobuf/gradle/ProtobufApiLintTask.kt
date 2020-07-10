package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.apilinter.ApiLinterRunner
import com.bybutter.sisyphus.jackson.parseJson
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

open class ProtobufApiLintTask : SourceTask() {

    @get:InputDirectory
    lateinit var protoPath: File

    @get:Internal
    lateinit var protobuf: ProtobufExtension

    @TaskAction
    fun apiLinter() {
        val cmd = mutableListOf<String>()
        val apiLinterConfig = protobuf.linter
        ruleHandle(cmd, apiLinterConfig)
        val path = protoPath.toPath().toString()
        cmd.addAll(listOf(OUTPUT_FORMAT, "json", PROTO_PATH, path))
        val excludeFiles = apiLinterConfig.excludeFiles
        Paths.get(path, "protosrc").toFile().bufferedReader().forEachLine {
            if (excludeFiles.isEmpty() || !excludeFiles.contains(it)) {
                cmd.add(it)
            }
        }
        val protoPathMapping = mutableMapOf<String, String>()
        Paths.get(path, "protofile").toFile().bufferedReader().forEachLine {
            val mapping = it.split("=", limit = 2)
            protoPathMapping[mapping[0]] = mapping[1]
        }
        val version = apiLinterConfig.version ?: ApiLinterRunner.API_LINTER_DEFAULT_VERSION
        val message = ApiLinterRunner().runApiLinter(cmd, version) ?: return
        printMessage(message.parseJson(), protoPathMapping)
        outputMessageToFile(message)
    }

    private fun outputMessageToFile(message: String) {
        val outPutDirectory = project.layout.buildDirectory.file(project.provider {
            "reports/apilinter"
        }).get().asFile.toPath()
        Files.createDirectory(outPutDirectory)
        Files.write(Paths.get(outPutDirectory.toString(), "apilinter.json"), message.toByteArray())
    }

    private fun ruleHandle(cmd: MutableList<String>, apiLinterConfig: ApiLinterConfig) {
        val enableRules = apiLinterConfig.enableRules
        if (enableRules.isNotEmpty()) {
            cmd.add(ENABLE_RULE)
            enableRules.forEach { cmd.add(it) }
        }
        val disableRules = apiLinterConfig.disableRules
        if (disableRules.isNotEmpty()) {
            cmd.add(DISABLE_RULE)
            disableRules.forEach { cmd.add(it) }
        }
    }

    private fun printMessage(linterResponseList: List<LinterResponse>, protoPathMapping: Map<String, String>) {
        for (linterResponse in linterResponseList) {
            linterResponse.problems.forEach {
                println("api-linter: ${protoPathMapping[linterResponse.filePath]}:${it.location.startPosition["line_number"]}:${it.location.startPosition["column_number"]}  ${it.message} rule detail in ${it.ruleDocUri}")
            }
        }
    }

    companion object {
        private const val DISABLE_RULE = "--disable-rule"
        private const val ENABLE_RULE = "--enable-rule"
        private const val PROTO_PATH = "--proto-path"
        private const val OUTPUT_FORMAT = "--output-format"
    }
}

data class LinterResponse(@JsonProperty("file_path") var filePath: String, @JsonProperty("problems") var problems: List<Problem>)

data class Problem(var message: String, var location: Location, @JsonProperty("rule_id") var ruleId: String, @JsonProperty("rule_doc_uri") var ruleDocUri: String)

data class Location(@JsonProperty("start_position") val startPosition: Map<String, String>, @JsonProperty("end_position") val endPosition: Map<String, String>)
