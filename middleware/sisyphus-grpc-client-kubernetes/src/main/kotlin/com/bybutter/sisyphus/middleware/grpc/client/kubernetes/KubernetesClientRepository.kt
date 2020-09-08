package com.bybutter.sisyphus.middleware.grpc.client.kubernetes

import com.bybutter.sisyphus.middleware.grpc.ClientBuilderInterceptor
import com.bybutter.sisyphus.middleware.grpc.ClientRepository
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.rpc.CallOptionsInterceptor
import com.bybutter.sisyphus.spring.BeanUtils
import io.grpc.CallOptions
import io.grpc.ClientInterceptor
import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.Config
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.env.Environment

class KubernetesClientRepository : ClientRepository {

    override var order: Int = Int.MIN_VALUE + 2000

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition> {
        val path = Paths.get(Config.SERVICEACCOUNT_ROOT, "namespace")
        if (!Files.exists(path)) {
            logger.warn("Skip discovering services on kubernetes cluster, kubernetes downward API file not found.")
            return emptyList()
        }
        val namespace = String(Files.readAllBytes(path), Charset.defaultCharset())
        val api = try {
            CoreV1Api(Config.fromCluster())
        } catch (e: Exception) {
            logger.warn("Skip discovering services on kubernetes cluster, an ${e.javaClass.name}('${e.message}') occurred when creating kubernetes client.")
            return emptyList()
        }
        logger.debug("Detect application is running in kubernetes namespace $namespace.")
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        val registerServiceNames = ProtoTypes.getRegisteredServiceNames()
        for (serviceName in registerServiceNames) {
            val list = try {
                api.listNamespacedService(namespace, null, null, null, null, "sisyphus/$serviceName", null, null, null, null)
            } catch (e: ApiException) {
                logger.error("An exception('${e.responseBody}') occurred when listing kubernetes services in namespace '$namespace'.")
                continue
            }
            if (list.items.isEmpty()) continue
            val k8sService = list.items[0]
            val labelValue = k8sService.metadata?.labels?.get("sisyphus/$serviceName") ?: continue
            val port = k8sService.spec?.ports?.first {
                it.name == labelValue || it.port.toString() == labelValue
            }?.port ?: continue
            val host = k8sService.metadata?.name ?: continue
            logger.info("GRPC service '$serviceName' discovered in kubernetes service '$host:$port'.")
            val channel = createGrpcChannel("$host:$port", beanFactory)
            val service = ProtoTypes.getRegisterService(serviceName) ?: continue
            val client = getClientFromService(service)
            val stub = getStubFromService(service)
            val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(client as Class<Any>) {
                val builderInterceptors = BeanUtils.getSortedBeans(beanFactory, ClientBuilderInterceptor::class.java)
                val clientInterceptors = BeanUtils.getSortedBeans(beanFactory, ClientInterceptor::class.java)
                val optionsInterceptors = BeanUtils.getSortedBeans(beanFactory, CallOptionsInterceptor::class.java)
                interceptStub(createGrpcClient(stub, channel, optionsInterceptors.values, CallOptions.DEFAULT), builderInterceptors.values, clientInterceptors.values)
            }
            beanDefinitionList.add(clientBeanDefinition.beanDefinition)
        }
        return beanDefinitionList
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this.javaClass)
    }
}
