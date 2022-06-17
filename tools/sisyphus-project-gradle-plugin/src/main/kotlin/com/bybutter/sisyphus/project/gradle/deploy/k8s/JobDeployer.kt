package com.bybutter.sisyphus.project.gradle.deploy.k8s

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.models.V1Job
import okhttp3.Call
import org.gradle.api.logging.Logger

class JobDeployer(
    environment: KubernetesEnvironment,
    tag: String,
    resource: KubernetesEnvironment.Resource,
    logger: Logger
) : KubernetesDeployerBase<V1Job>(environment, tag, resource, logger) {
    private val client = BatchV1Api(environment.api)

    override fun kind() = "Job"

    override fun resourceClass() = V1Job::class.java

    override fun readResource() = client.readNamespacedJob(resource.name, resource.namespace, null)

    override fun checkReady(resource: V1Job): Boolean {
        return (resource.status?.active ?: 0) > 0 || (resource.status?.succeeded ?: 0) > 0 ||
            (resource.status?.failed ?: 0) > 0
    }

    override fun patchResource(resource: V1Job) {
        patchMetadata(resource.spec?.template?.metadata)
        patchContainers(resource.spec?.template?.spec?.containers ?: listOf())
    }

    override fun patchResourceCall(patch: V1Patch): Call {
        return client.patchNamespacedJobCall(
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
