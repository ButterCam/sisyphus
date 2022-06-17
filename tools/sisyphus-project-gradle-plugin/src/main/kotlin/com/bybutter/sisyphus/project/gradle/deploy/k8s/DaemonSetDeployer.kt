package com.bybutter.sisyphus.project.gradle.deploy.k8s

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.V1DaemonSet
import okhttp3.Call
import org.gradle.api.logging.Logger

class DaemonSetDeployer(
    environment: KubernetesEnvironment,
    tag: String,
    resource: KubernetesEnvironment.Resource,
    logger: Logger
) : KubernetesDeployerBase<V1DaemonSet>(environment, tag, resource, logger) {
    private val client = AppsV1Api(environment.api)

    override fun kind() = "DaemonSet"

    override fun resourceClass() = V1DaemonSet::class.java

    override fun readResource() = client.readNamespacedDaemonSet(resource.name, resource.namespace, null)

    override fun patchResource(resource: V1DaemonSet) {
        patchMetadata(resource.spec?.template?.metadata)
        patchContainers(resource.spec?.template?.spec?.containers ?: listOf())
    }

    override fun checkReady(resource: V1DaemonSet): Boolean {
        return (resource.status?.numberUnavailable ?: 0) == 0
    }

    override fun patchResourceCall(patch: V1Patch): Call {
        return client.patchNamespacedDaemonSetCall(
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
