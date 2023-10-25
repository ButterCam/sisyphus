package org.gradle.kotlin.dsl

import com.bybutter.sisyphus.k8s.gradle.KubernetesResource
import com.bybutter.sisyphus.k8s.gradle.resource.KubernetesResourceSupport
import io.kubernetes.client.openapi.models.V1Deployment
import org.gradle.util.internal.GUtil

fun KubernetesResource<V1Deployment>.deploying(
    name: String = "",
    block: V1Deployment.() -> Unit,
) {
    val project = this.cluster.extension.project
    val support = KubernetesResourceSupport.fromKind(this.kind) as KubernetesResourceSupport<V1Deployment>
    project.tasks.create(name.ifBlank { deployTaskName() }) {
        it.group = "deploying"
        it.description = deployTaskDescription()
        it.doFirst {
            support.deploy(this, it, block)
            support.waitForReady(this, it)
        }
    }
}

private fun <T> KubernetesResource<T>.deployTaskName(): String {
    return "deploy$kind${GUtil.toCamelCase(name)}On${GUtil.toCamelCase(cluster.name)}"
}

private fun <T> KubernetesResource<T>.deployTaskDescription(): String {
    return "deploy ${GUtil.toLowerCamelCase(kind)} $name On ${cluster.name}"
}
