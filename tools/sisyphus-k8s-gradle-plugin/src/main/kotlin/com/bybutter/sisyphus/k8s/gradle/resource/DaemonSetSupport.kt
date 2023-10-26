package com.bybutter.sisyphus.k8s.gradle.resource

import com.bybutter.sisyphus.k8s.gradle.KubernetesResource
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.V1DaemonSet
import okhttp3.Call
import org.gradle.api.Task
import org.gradle.util.internal.GUtil

object DaemonSetSupport : KubernetesResourceSupport<V1DaemonSet> {
    override val kind: String = "DaemonSet"
    override val alias: Set<String> = setOf("daemonset", "daemonsets")
    override val resourceClass: Class<V1DaemonSet> = V1DaemonSet::class.java

    override fun patchResourceCall(
        resource: KubernetesResource<V1DaemonSet>,
        patch: V1Patch,
    ): Call {
        val api = AppsV1Api(resource.cluster.api)
        return api.patchNamespacedDaemonSetCall(
            resource.name, resource.namespace, patch, null, null, "kubectl-rollout", null, null, null,
        )
    }

    override fun updateMetadata(
        resource: KubernetesResource<V1DaemonSet>,
        k8sResource: V1DaemonSet,
    ) {
        patchRestartMetadata(k8sResource.spec?.template?.metadata)
    }

    override fun getResource(resource: KubernetesResource<V1DaemonSet>): V1DaemonSet {
        val api = AppsV1Api(resource.cluster.api)
        return api.readNamespacedDaemonSet(resource.name, resource.namespace, null)
    }

    override fun checkReady(
        resource: KubernetesResource<V1DaemonSet>,
        task: Task,
        k8sResource: V1DaemonSet,
    ): Boolean {
        task.logger.lifecycle(
            "Waiting for ${
                GUtil.toCamelCase(
                    kind,
                )
            } ${resource.namespace}/${resource.name} to be ready(${
                k8sResource.status?.numberUnavailable
            }/${k8sResource.status?.numberAvailable}).",
        )
        return (k8sResource.status?.numberUnavailable ?: 0) == 0
    }
}
