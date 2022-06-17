package com.bybutter.sisyphus.project.gradle.deploy.k8s

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.V1Deployment
import okhttp3.Call
import org.gradle.api.logging.Logger

class DeploymentDeployer(
    environment: KubernetesEnvironment,
    tag: String,
    resource: KubernetesEnvironment.Resource,
    logger: Logger
) : KubernetesDeployerBase<V1Deployment>(environment, tag, resource, logger) {
    private val client = AppsV1Api(environment.api)

    override fun kind() = "Deployment"

    override fun resourceClass() = V1Deployment::class.java

    override fun readResource() = client.readNamespacedDeployment(resource.name, resource.namespace, null)

    override fun patchResource(resource: V1Deployment) {
        patchMetadata(resource.spec?.template?.metadata)
        patchContainers(resource.spec?.template?.spec?.containers ?: listOf())
    }

    override fun checkReady(resource: V1Deployment): Boolean {
        return (resource.status?.unavailableReplicas ?: 0) == 0
    }

    override fun patchResourceCall(patch: V1Patch): Call {
        return client.patchNamespacedDeploymentCall(
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
