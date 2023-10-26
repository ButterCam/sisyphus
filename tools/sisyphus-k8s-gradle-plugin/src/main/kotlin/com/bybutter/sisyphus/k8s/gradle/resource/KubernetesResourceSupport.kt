package com.bybutter.sisyphus.k8s.gradle.resource

import com.bybutter.sisyphus.k8s.gradle.KubernetesResource
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.util.PatchUtils
import io.kubernetes.client.util.wait.Wait
import okhttp3.Call
import org.gradle.api.Task
import java.time.Duration

interface KubernetesResourceSupport<T> {
    val kind: String
    val alias: Set<String>
    val resourceClass: Class<T>

    fun deploy(
        resource: KubernetesResource<T>,
        task: Task,
        patcher: T.() -> Unit,
    ) {
        val k8sResource = getResource(resource)
        updateMetadata(resource, k8sResource)
        patcher(k8sResource)

        PatchUtils.patch(
            resourceClass,
            {
                val patch = V1Patch(resource.cluster.api.json.serialize(k8sResource))
                patchResourceCall(resource, patch)
            },
            V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH,
            resource.cluster.api,
        )
    }

    fun waitForReady(
        resource: KubernetesResource<T>,
        task: Task,
    ) {
        val result =
            Wait.poll(Duration.ofSeconds(3), Duration.ofSeconds(300)) {
                try {
                    getResource(resource)?.let { checkReady(resource, task, it) } ?: false
                } catch (e: Exception) {
                    false
                }
            }
        if (!result) {
            task.logger.lifecycle("$kind ${resource.namespace}/${resource.name} startup timeout.")
        }
    }

    fun getResource(resource: KubernetesResource<T>): T

    fun updateMetadata(
        resource: KubernetesResource<T>,
        k8sResource: T,
    )

    fun patchResourceCall(
        resource: KubernetesResource<T>,
        patch: V1Patch,
    ): Call

    fun checkReady(
        resource: KubernetesResource<T>,
        task: Task,
        k8sResource: T,
    ): Boolean

    companion object {
        fun fromKind(kind: String): KubernetesResourceSupport<*> {
            return when (kind) {
                DeploymentSupport.kind -> DeploymentSupport
                DaemonSetSupport.kind -> DaemonSetSupport
                StatefulSetSupport.kind -> StatefulSetSupport
                in DeploymentSupport.alias -> DeploymentSupport
                in DaemonSetSupport.alias -> DaemonSetSupport
                in StatefulSetSupport.alias -> StatefulSetSupport
                else -> throw IllegalArgumentException("Unsupported kind: $kind")
            }
        }
    }
}
