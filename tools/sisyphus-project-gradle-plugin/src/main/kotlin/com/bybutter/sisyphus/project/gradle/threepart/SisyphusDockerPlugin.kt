package com.bybutter.sisyphus.project.gradle.threepart

import com.bybutter.sisyphus.project.gradle.SisyphusExtension
import com.bybutter.sisyphus.project.gradle.ensurePlugin
import com.bybutter.sisyphus.project.gradle.tryApplyPluginClass
import com.palantir.gradle.docker.DockerExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask

class SisyphusDockerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("application", ::apply) {
            return
        }

        val dockerFile = target.projectDir.resolve("Dockerfile")
        if (!dockerFile.exists()) return
        if (!target.tryApplyPluginClass("com.palantir.gradle.docker.PalantirDockerPlugin") {
                target.afterEvaluate { afterEvaluate(it) }
            }) return
        val docker = target.extensions.getByType(DockerExtension::class.java)
        docker.name = "${target.name}:${target.version}"

        var project: Project? = target
        while (project != null) {
            val dockerDir = project.projectDir.resolve("docker")
            if (dockerDir.exists() && dockerDir.isDirectory) {
                docker.files(dockerDir)
            }
            project = project.parent
        }

        val sisyphus = target.extensions.getByType(SisyphusExtension::class.java)
        for (registry in sisyphus.dockerPublishRegistries) {
            val registryInfo = sisyphus.repositories[registry] ?: continue
            docker.tag(registry, "${registryInfo.url}/${docker.name}")
        }
    }

    private fun afterEvaluate(target: Project) {
        val docker = target.extensions.getByType(DockerExtension::class.java)

        target.tasks.withType(AbstractArchiveTask::class.java) {
            docker.files(it.outputs)
        }

        docker.buildArgs(docker.buildArgs + mapOf(
            "PROJECT_NAME" to target.name,
            "PROJECT_VERSION" to target.version.toString()
        ))
    }
}
