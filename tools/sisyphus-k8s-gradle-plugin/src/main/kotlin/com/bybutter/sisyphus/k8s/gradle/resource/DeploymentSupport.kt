package com.bybutter.sisyphus.k8s.gradle.resource

import com.bybutter.sisyphus.k8s.gradle.KubernetesResource
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.V1Deployment
import okhttp3.Call
import org.gradle.api.Task
import org.gradle.util.internal.GUtil

object DeploymentSupport : KubernetesResourceSupport<V1Deployment> {
    override val kind: String = "Deployment"
    override val alias: Set<String> = setOf("deployment", "deployments", "deploy", "deploys")
    override val resourceClass: Class<V1Deployment> = V1Deployment::class.java

    override fun patchResourceCall(
        resource: KubernetesResource<V1Deployment>,
        patch: V1Patch,
    ): Call {
        val api = AppsV1Api(resource.cluster.api)
        return api.patchNamespacedDeploymentCall(
            resource.name, resource.namespace, patch, null, null, "kubectl-rollout", null, null, null,
        )
    }

    override fun updateMetadata(
        resource: KubernetesResource<V1Deployment>,
        k8sResource: V1Deployment,
    ) {
        patchRestartMetadata(k8sResource.spec?.template?.metadata)
    }

    override fun getResource(resource: KubernetesResource<V1Deployment>): V1Deployment {
        val api = AppsV1Api(resource.cluster.api)
        return api.readNamespacedDeployment(resource.name, resource.namespace, null)
    }

    override fun checkReady(
        resource: KubernetesResource<V1Deployment>,
        task: Task,
        k8sResource: V1Deployment,
    ): Boolean {
        task.logger.lifecycle(
            "Waiting for ${GUtil.toCamelCase(
                kind,
            )} ${resource.namespace}/${resource.name} to be ready(${k8sResource.status?.readyReplicas}/${k8sResource.status?.replicas}).",
        )
        return (k8sResource.status?.unavailableReplicas ?: 0) == 0
    }
}
