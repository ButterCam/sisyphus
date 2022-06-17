package com.bybutter.sisyphus.project.gradle.deploy.k8s

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.V1StatefulSet
import okhttp3.Call
import org.gradle.api.logging.Logger

class StatefulSetDeployer(
    environment: KubernetesEnvironment,
    tag: String,
    resource: KubernetesEnvironment.Resource,
    logger: Logger
) : KubernetesDeployerBase<V1StatefulSet>(environment, tag, resource, logger) {
    private val client = AppsV1Api(environment.api)

    override fun kind() = "StatefulSet"

    override fun resourceClass() = V1StatefulSet::class.java

    override fun readResource() = client.readNamespacedStatefulSet(resource.name, resource.namespace, null)

    override fun checkReady(resource: V1StatefulSet): Boolean {
        return resource.status?.currentReplicas == resource.status?.replicas
    }

    override fun patchResource(resource: V1StatefulSet) {
        patchMetadata(resource.spec?.template?.metadata)
        patchContainers(resource.spec?.template?.spec?.containers ?: listOf())
    }

    override fun patchResourceCall(patch: V1Patch): Call {
        return client.patchNamespacedStatefulSetCall(
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
