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
        if (!target.tryApplyPluginClass("com.palantir.gradle.docker.PalantirDockerPlugin")) return
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

        target.tasks.withType(AbstractArchiveTask::class.java) {
            it.outputs
        }

        val sisyphus = target.extensions.getByType(SisyphusExtension::class.java)
        for (registry in sisyphus.dockerPublishRegistries) {
            val registryInfo = sisyphus.repositories[registry] ?: continue
            docker.tag(registry, "${registryInfo.url}/${docker.name}")
        }
    }
}