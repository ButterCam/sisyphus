package com.bybutter.sisyphus.project.gradle.deploy.k8s

import com.bybutter.sisyphus.project.gradle.deploy.DeployingExtension
import com.bybutter.sisyphus.project.gradle.ensurePlugin
import com.palantir.gradle.docker.DockerExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.internal.GUtil

class KubernetesDeployPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("application", "com.palantir.docker") {
            apply(it)
        }.also {
            if (!it) return
        }

        target.afterEvaluate { _ ->
            val docker = target.extensions.getByType(DockerExtension::class.java)

            val tags = docker.namedTags
            if (tags.isEmpty()) return@afterEvaluate

            val deploying = target.extensions.getByType(DeployingExtension::class.java)
            val environments = deploying.environments
            if (environments.isEmpty()) return@afterEvaluate

            environments.keys.forEach {
                val camelCase = GUtil.toCamelCase(it)
                val environment = environments[it] as? KubernetesEnvironment ?: return@forEach
                val resources = environment.resources()
                if (resources.isEmpty()) return@forEach
                val tagName = environment.boundTag.takeIf { it.isNotEmpty() } ?: it
                val taskCamelCase = GUtil.toCamelCase(tagName)
                val tag = tags[tagName]?.let {
                    environment.rewriteTagFun?.invoke(it) ?: it
                } ?: return@forEach

                target.tasks.create("deployTo$camelCase", KubernetesDeployTask::class.java) {
                    it.environment = environment
                    it.tag = tag
                    it.group = "deploying"
                    it.description = "Deploy $tag to ${resources.joinToString(", ")} on k8s cluster"
                    it.dependsOn(target.tasks.getByName("dockerPush$taskCamelCase"))
                }
            }
        }
    }
}
