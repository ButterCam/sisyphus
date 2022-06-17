package com.bybutter.sisyphus.project.gradle.deploy.k8s

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.models.V1Container
import io.kubernetes.client.openapi.models.V1ObjectMeta
import io.kubernetes.client.util.PatchUtils
import io.kubernetes.client.util.wait.Wait
import okhttp3.Call
import org.gradle.api.logging.Logger
import org.gradle.util.internal.GUtil
import java.time.Duration
import java.time.LocalDateTime

abstract class KubernetesDeployerBase<T>(
    protected val environment: KubernetesEnvironment,
    protected val tag: String,
    protected val resource: KubernetesEnvironment.Resource,
    private val logger: Logger
) : KubernetesDeployer {
    protected abstract fun kind(): String

    protected abstract fun resourceClass(): Class<T>

    protected abstract fun readResource(): T?

    protected abstract fun patchResource(resource: T)

    protected abstract fun patchResourceCall(patch: V1Patch): Call

    protected abstract fun checkReady(resource: T): Boolean

    protected open fun patchContainers(containers: List<V1Container>) {
        if (containers.size > 1 && resource.container == null) {
            throw IllegalArgumentException(
                "Deployment ${resource.namespace}/${resource.name} has multiple containers, " + "but container is not specified."
            )
        }
        containers.forEach {
            if (resource.container == null || it.name == resource.container) {
                it.image = tag
            }
        }
    }

    protected open fun patchMetadata(metadata: V1ObjectMeta?) {
        metadata?.putAnnotationsItem(
            "kubectl.kubernetes.io/restartedAt", LocalDateTime.now().toString()
        )
    }

    override fun deploy() {
        val app = readResource()
        if (app == null) {
            logger.warn("${kind()} ${resource.namespace}/${resource.name} not found, skip deploying.")
            return
        }
        patchResource(app)
        PatchUtils.patch(
            resourceClass(),
            {
                val patch = V1Patch(environment.api.json.serialize(app))
                patchResourceCall(patch)
            },
            V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH, environment.api
        )
        val result = Wait.poll(Duration.ofSeconds(3), Duration.ofSeconds(300)) {
            try {
                logger.lifecycle("Waiting for (${GUtil.toCamelCase(kind())}) ${resource.namespace}/${resource.name} to be ready.")
                readResource()?.let { checkReady(it) } ?: false
            } catch (e: Exception) {
                false
            }
        }
        if (!result) {
            logger.lifecycle("Waiting for (${GUtil.toCamelCase(kind())}) ${resource.namespace}/${resource.name} startup timeout.")
        }
    }
}
