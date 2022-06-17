package com.bybutter.sisyphus.project.gradle.deploy

import com.bybutter.sisyphus.project.gradle.deploy.k8s.KubernetesEnvironment
import org.gradle.api.Project
import java.net.URI

open class DeployingExtension(val project: Project) {
    val environments = mutableMapOf<String, Environment>()

    fun k8s(name: String, block: KubernetesEnvironment.() -> Unit) {
        val current = environments.getOrPut(name) {
            KubernetesEnvironment()
        }
        if (current !is KubernetesEnvironment) {
            throw IllegalArgumentException("Environment $name already exists, but is not a KubernetesEnvironment")
        }
        block(current)
    }

    init {
        for (key in project.properties.keys) {
            val result = environmentRegex.matchEntire(key) ?: continue
            val envName = result.groupValues[1]

            val env = URI(project.findProperty(key) as? String ?: continue)
            when (env.scheme) {
                "k8s" -> {
                    val data = env.authority.split(':')
                    k8s(envName) {
                        when (data.size) {
                            1 -> kubeConfig(data[0])
                            2 -> kubeConfig(data[0], data[1])
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val environmentRegex = """sisyphus\.deploy\.environment\.([A-Za-z][A-Za-z0-9-_]+)""".toRegex()
    }
}
