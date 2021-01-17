package com.bybutter.sisyphus.middleware.grpc.client.kubernetes

import com.bybutter.sisyphus.jackson.toJson
import com.bybutter.sisyphus.middleware.grpc.RpcServiceImpl
import com.bybutter.sisyphus.middleware.grpc.client.kubernetes.support.PatchOperateType
import com.bybutter.sisyphus.middleware.grpc.client.kubernetes.support.ServiceLabelPatch
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Service
import io.kubernetes.client.util.Config
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportAware
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata

class KubernetesLabelAutoRegister : ApplicationListener<ApplicationReadyEvent>, ImportAware, EnvironmentAware {

    private lateinit var environment: Environment
    private lateinit var importMetadata: AnnotationMetadata

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun setImportMetadata(importMetadata: AnnotationMetadata) {
        this.importMetadata = importMetadata
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val beanFactory = event.applicationContext.beanFactory
        val path = Paths.get(Config.SERVICEACCOUNT_ROOT, "namespace")
        if (!Files.exists(path)) {
            logger.warn("Skip kubernetes label auto register on kubernetes cluster, kubernetes downward API file not found.")
            return
        }
        val namespace = String(Files.readAllBytes(path), Charset.defaultCharset())
        val coreV1Api = try {
            CoreV1Api(Config.fromCluster())
        } catch (e: Exception) {
            logger.warn("Skip kubernetes label auto register on kubernetes cluster, an ${e.javaClass.name}('${e.message}') occurred when creating kubernetes client.")
            return
        }
        val enableAnnotation = importMetadata.getAnnotationAttributes(EnableKubernetesServiceDiscovery::class.java.name)
                ?: return
        val serviceName = (enableAnnotation[EnableKubernetesServiceDiscovery::discoveryName.name] as? String) ?: return
        val service = try {
            coreV1Api.readNamespacedService(serviceName, namespace, null, null, null)
        } catch (e: ApiException) {
            logger.warn("Skip kubernetes label auto register, get service fail : ${e.responseBody}")
            return
        }
        val serviceLabelPatchList = handleServiceLabels(service, enableAnnotation, beanFactory) ?: return
        try {
            coreV1Api.patchNamespacedService(service.metadata?.name, namespace, V1Patch(serviceLabelPatchList.toJson()), null, null, null, null)
        } catch (e: ApiException) {
            logger.error("Patch kubernetes label fail patch body is ${serviceLabelPatchList.toJson()}, error is : ${e.responseBody}")
        }
    }

    private fun handleServiceLabels(service: V1Service, enableAnnotation: Map<String, Any>, beanFactory: ConfigurableListableBeanFactory): Set<ServiceLabelPatch>? {
        val enableServices = (enableAnnotation[EnableKubernetesServiceDiscovery::services.name] as? Array<String>)?.asList()
                ?: listOf()
        val override: Boolean = (enableAnnotation[EnableKubernetesServiceDiscovery::override.name] as? Boolean) ?: false
        val serverServices = beanFactory.getBeansWithAnnotation(RpcServiceImpl::class.java)
        val labels = (service.metadata?.labels
                ?: mutableMapOf()).filter { it.key.startsWith("sisyphus/") }
        val serviceLabelPatchList = mutableSetOf<ServiceLabelPatch>()
        for ((_, serverService) in serverServices) {
            val serviceName = "${rpcServiceAnnotation.parent}.${rpcServiceAnnotation.value}"
            if (enableServices.isEmpty() || enableServices.contains(serviceName)) {
                val labelKey = "sisyphus~1$serviceName"
                val labelValue = environment.getProperty(GrpcServerConstants.GRPC_PORT_PROPERTY, Int::class.java, GrpcServerConstants.DEFAULT_GRPC_PORT).toString()
                generateKubernetesLabelPatch(override, labels, labelKey, labelValue)?.let {
                    serviceLabelPatchList.add(it)
                }
            }
        }
        return serviceLabelPatchList
    }

    private fun generateKubernetesLabelPatch(override: Boolean, labels: Map<String, String>, labelKey: String, labelValue: String): ServiceLabelPatch? {
        return if (labels.containsKey(labelKey)) {
            if (override) {
                ServiceLabelPatch(PatchOperateType.REPLACE, labelKey, labelValue)
            } else {
                null
            }
        } else {
            ServiceLabelPatch(PatchOperateType.ADD, labelKey, labelValue)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this.javaClass)
    }
}
