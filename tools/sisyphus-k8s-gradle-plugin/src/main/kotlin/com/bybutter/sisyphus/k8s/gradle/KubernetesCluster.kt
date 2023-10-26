package com.bybutter.sisyphus.k8s.gradle

import com.bybutter.sisyphus.k8s.gradle.resource.KubernetesResourceSupport
import io.kubernetes.client.common.KubernetesObject
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.models.V1DaemonSet
import io.kubernetes.client.openapi.models.V1Deployment
import io.kubernetes.client.openapi.models.V1StatefulSet
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.KubeConfig
import org.jetbrains.kotlin.com.google.common.io.Files
import java.io.File
import java.io.IOException

class KubernetesCluster(val name: String, val extension: KubernetesExtension) {
    var configPath: String? = null

    var contextName: String? = null

    val resources = mutableMapOf<String, KubernetesResource<*>>()

    internal val api: ApiClient by lazy {
        detectK8sConfig()
    }

    private fun detectK8sConfig(): ApiClient {
        val config = configPath
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
        } else {
            val kube =
                Files.newReader(File(config), Charsets.UTF_8).use {
                    KubeConfig.loadKubeConfig(it).apply {
                        if (contextName != null) {
                            this.setContext(contextName)
                        }
                    }
                }
            return ClientBuilder.kubeconfig(kube).build()
        }
    }

    private fun resourceInfo(name: String): Pair<String, String> {
        val data = name.split('/')
        return when (data.size) {
            1 -> "" to name
            2 -> data[0] to data[1]
            else -> throw IllegalArgumentException("Invalid resource name: $name")
        }
    }

    fun <T : KubernetesObject> resource(
        kind: String,
        name: String,
        block: KubernetesResource<T>.() -> Unit,
    ) {
        val (namespace, resourceName) = resourceInfo(name)
        val current =
            resources.getOrPut("$kind:$name") {
                KubernetesResource<T>(this, KubernetesResourceSupport.fromKind(kind).kind, namespace, resourceName)
            }

        if (current.kind != kind) {
            throw IllegalArgumentException("Resource $name is already defined as ${current.kind}")
        }

        block(current as KubernetesResource<T>)
    }

    fun deployment(
        name: String,
        block: KubernetesResource<V1Deployment>.() -> Unit,
    ) {
        resource("Deployment", name, block)
    }

    fun statefulSet(
        name: String,
        block: KubernetesResource<V1StatefulSet>.() -> Unit,
    ) {
        resource("StatefulSet", name, block)
    }

    fun deamonSet(
        name: String,
        block: KubernetesResource<V1DaemonSet>.() -> Unit,
    ) {
        resource("DaemonSet", name, block)
    }
}
