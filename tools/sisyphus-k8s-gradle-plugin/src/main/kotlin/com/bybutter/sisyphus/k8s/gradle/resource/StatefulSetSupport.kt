package com.bybutter.sisyphus.k8s.gradle.resource

import com.bybutter.sisyphus.k8s.gradle.KubernetesResource
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.V1StatefulSet
import okhttp3.Call
import org.gradle.api.Task
import org.gradle.util.internal.GUtil

object StatefulSetSupport : KubernetesResourceSupport<V1StatefulSet> {
    override val kind: String = "StatefulSet"
    override val alias: Set<String> = setOf("statefulset", "statefulsets")
    override val resourceClass: Class<V1StatefulSet> = V1StatefulSet::class.java

    override fun patchResourceCall(
        resource: KubernetesResource<V1StatefulSet>,
        patch: V1Patch,
    ): Call {
        val api = AppsV1Api(resource.cluster.api)
        return api.patchNamespacedStatefulSetCall(
            resource.name,
            resource.namespace,
            patch,
            null,
            null,
            "kubectl-rollout",
            null,
            null,
            null,
        )
    }

    override fun updateMetadata(
        resource: KubernetesResource<V1StatefulSet>,
        k8sResource: V1StatefulSet,
    ) {
        patchRestartMetadata(k8sResource.spec?.template?.metadata)
    }

    override fun getResource(resource: KubernetesResource<V1StatefulSet>): V1StatefulSet {
        val api = AppsV1Api(resource.cluster.api)
        return api.readNamespacedStatefulSet(resource.name, resource.namespace, null)
    }

    override fun checkReady(
        resource: KubernetesResource<V1StatefulSet>,
        task: Task,
        k8sResource: V1StatefulSet,
    ): Boolean {
        task.logger.lifecycle(
            "Waiting for ${GUtil.toCamelCase(
                kind,
            )} ${resource.namespace}/${resource.name} to be ready(${k8sResource.status?.readyReplicas}/${k8sResource.status?.replicas}).",
        )
        return k8sResource.status?.currentReplicas == k8sResource.status?.replicas
    }
}
