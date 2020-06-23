package com.bybutter.sisyphus.middleware.grpc.client.kubernetes

import com.bybutter.sisyphus.middleware.grpc.ClientRepository
import com.bybutter.sisyphus.protobuf.ProtoTypes
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

    companion object {
        private val logger = LoggerFactory.getLogger(this.javaClass)
    }

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition> {
        val api = try {
            CoreV1Api(Config.fromCluster())
        } catch (e: IllegalStateException) {
            logger.warn("Due to kubernetes environment not found, skip discover service on kubernetes cluster")
            return emptyList()
        } catch (e: NullPointerException) {
            logger.warn("Due to kubernetes environment not found, skip discover service on kubernetes cluster")
            return emptyList()
        }
        val path = Paths.get(Config.SERVICEACCOUNT_ROOT, "namespace")
        if (!Files.exists(path)) {
            logger.warn("Due to Can not find kubernetes namespace file ${path.fileName}, skip discover service on kubernetes cluster")
            return emptyList()
        }
        val namespace = String(Files.readAllBytes(path), Charset.defaultCharset())
        logger.info("Detect application running in kubernetes namespace $namespace.")
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        val registerServiceNames = ProtoTypes.getRegisteredServiceNames()
        for (serviceName in registerServiceNames) {
            val list = try {
                api.listNamespacedService(namespace, null, null, null, null, serviceName, null, null, null, null)
            } catch (e: ApiException) {
                logger.error("Get api exception '${e.message}' when list kubernetes services in namespace '${e.responseBody}'.")
                continue
            }
            if (list.items.isEmpty()) {
                continue
            }
            val k8sService = list.items[0]
            val labelValue = k8sService.metadata?.labels?.get(serviceName) ?: continue
            val port = k8sService.spec?.ports?.first {
                it.name == labelValue || it.port.toString() == labelValue
            }?.port ?: continue
            val host = k8sService.metadata?.name ?: continue
            logger.info("Discover gRPC service '${list.items[0].metadata?.name}' on kubernetes '$host:$port'.")
            val channel = createGrpcChannel(host, port)
            val service = ProtoTypes.getRegisterService(serviceName) ?: continue
            val client = getClientFromService(service)
            val stub = getStubFromService(service)
            val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(client as Class<Any>) {
                processStub(createGrpcClient(stub, channel), beanFactory)
            }
            beanDefinitionList.add(clientBeanDefinition.beanDefinition)
        }
        return beanDefinitionList
    }
}
