package com.bybutter.sisyphus.project.gradle.deploy.k8s

import com.bybutter.sisyphus.project.gradle.deploy.Environment
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.KubeConfig
import org.jetbrains.kotlin.com.google.common.io.Files
import java.io.File
import java.io.IOException

class KubernetesEnvironment : Environment {
    private var config: Config? = null

    private val resources: MutableSet<Resource> = mutableSetOf()

    internal val api: ApiClient by lazy {
        detectK8sConfig()
    }

    var boundTag: String = ""

    internal var rewriteTagFun: ((String) -> String)? = null

    fun rewriteTag(func: ((String) -> String)) {
        rewriteTagFun = func
    }

    fun resource(kind: String, resource: String, container: String? = null) {
        val data = resource.split('/')
        if (data.size != 2) {
            throw IllegalArgumentException("Invalid resource format: $resource")
        }
        resources.add(Resource(kind, data.first(), data.last(), container))
    }

    fun resource(kind: String, namespace: String, name: String, container: String? = null) {
        resources.add(Resource(kind, namespace, name, container))
    }

    fun resources(): Set<Resource> = resources.toSet()

    fun kubeConfig(config: String = "", context: String = "") {
        this.config = Config.KubeConfig(config, context)
    }

    fun cluster() {
        this.config = Config.InCluster
    }

    data class Resource(val kind: String, val namespace: String, val name: String, val container: String?) {
        override fun toString(): String {
            return if (container == null) {
                "$namespace/$name"
            } else {
                "$namespace/$name/$container"
            }
        }
    }

    sealed interface Config {
        object InCluster : Config

        class KubeConfig(val config: String = "", val context: String) : Config
    }

    private fun detectK8sConfig(): ApiClient {
        val config = config
        if (config == null) {
            try {
                return ClientBuilder.standard().build()
            } catch (e: IOException) {
                // ignore
            }
            try {
                return ClientBuilder.cluster().build()
            } catch (e: IOException) {
                // ignore
            }
            throw IllegalStateException("No kubernetes config found.")
        }

        return when (config) {
            Config.InCluster -> ClientBuilder.cluster().build()
            is Config.KubeConfig -> {
                val kube = Files.newReader(File(config.config), Charsets.UTF_8).use {
                    KubeConfig.loadKubeConfig(it).apply {
                        if (config.context.isNotBlank()) {
                            this.setContext(config.context)
                        }
                    }
                }
                ClientBuilder.kubeconfig(kube).build()
            }
        }
    }
}
