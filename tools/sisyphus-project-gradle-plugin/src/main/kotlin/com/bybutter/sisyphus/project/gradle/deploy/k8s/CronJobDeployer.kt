package com.bybutter.sisyphus.project.gradle.deploy.k8s

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.models.V1CronJob
import okhttp3.Call
import org.gradle.api.logging.Logger

class CronJobDeployer(
    environment: KubernetesEnvironment,
    tag: String,
    resource: KubernetesEnvironment.Resource,
    logger: Logger
) : KubernetesDeployerBase<V1CronJob>(environment, tag, resource, logger) {
    private val client = BatchV1Api(environment.api)

    override fun kind() = "CronJob"

    override fun resourceClass() = V1CronJob::class.java

    override fun readResource() = client.readNamespacedCronJob(resource.name, resource.namespace, null)

    override fun patchResource(resource: V1CronJob) {
        patchMetadata(resource.spec?.jobTemplate?.spec?.template?.metadata)
        patchContainers(resource.spec?.jobTemplate?.spec?.template?.spec?.containers ?: listOf())
    }

    override fun checkReady(resource: V1CronJob): Boolean {
        return true
    }

    override fun patchResourceCall(patch: V1Patch): Call {
        return client.patchNamespacedCronJobCall(
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
