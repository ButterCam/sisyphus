package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.api.Service
import com.bybutter.sisyphus.jackson.toYaml
import com.google.api.tools.framework.tools.configgen.ServiceConfigGeneratorTool
import java.io.File
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

open class ProtobufApiCompileTask : SourceTask() {
    @get:InputDirectory
    lateinit var protobufPath: File

    @get:OutputDirectory
    lateinit var resourceOutput: File

    @get:Internal
    var service: Service? = null

    @TaskAction
    fun compileApi() {
        val service = service ?: return
        val serviceConfig = protobufPath.resolve("service.yml")
        serviceConfig.writeText(serviceToYamlConfig(service))
        val descFile = protobufPath.resolve("protodesc.pb")
        val apiConfig = resourceOutput.resolve("api.json")

        ServiceConfigGeneratorTool.main(
            arrayOf(
                "--configs",
                serviceConfig.toString(),
                "--descriptor",
                descFile.toString(),
                "--json_out",
                apiConfig.toString()
            )
        )
    }

    private val yamlHeaderRegex = """^---\n""".toRegex()
    private fun serviceToYamlConfig(service: Service): String {
        return service.toYaml().replace(yamlHeaderRegex, "---\ntype: google.api.Service\n")
    }
}
