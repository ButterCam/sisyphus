package com.bybutter.sisyphus.project.gradle.deploy.k8s

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Pod
import okhttp3.Call
import org.gradle.api.logging.Logger

class PodDeployer(
    environment: KubernetesEnvironment,
    tag: String,
    resource: KubernetesEnvironment.Resource,
    logger: Logger
) : KubernetesDeployerBase<V1Pod>(environment, tag, resource, logger) {
    private val client = CoreV1Api(environment.api)

    override fun kind() = "Pod"

    override fun resourceClass() = V1Pod::class.java

    override fun readResource() = client.readNamespacedPod(resource.name, resource.namespace, null)

    override fun patchResource(resource: V1Pod) {
        patchMetadata(resource.metadata)
        patchContainers(resource.spec?.containers ?: listOf())
    }

    override fun checkReady(resource: V1Pod): Boolean {
        return resource.status?.containerStatuses?.all {
            it.ready
        } == true
    }

    override fun patchResourceCall(patch: V1Patch): Call {
        return client.patchNamespacedPodCall(
            resource.name,
            resource.namespace,
            patch,
            null,
            null,
            "kubectl-rollout",
            null,
            null,
            null
        )
    }
}
