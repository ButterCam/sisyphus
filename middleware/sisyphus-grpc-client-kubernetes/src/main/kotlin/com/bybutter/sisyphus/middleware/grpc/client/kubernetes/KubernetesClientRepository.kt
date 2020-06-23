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
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition> {
        val api = try {
            CoreV1Api(Config.fromCluster())
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Get config fail: $e, maybe not in k8s.")
        } catch (e: NullPointerException) {
            throw e
        }
        val path = Paths.get(Config.SERVICEACCOUNT_ROOT, "namespace")
        if (!Files.exists(path)) {
            throw IllegalStateException("can not find ${path.fileName}.")
        }
        val namespace = String(Files.readAllBytes(path), Charset.defaultCharset())
        logger.info("Kubernetes namespace is $namespace.")
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        val registerServiceNames = ProtoTypes.getRegisteredServiceNames()
        for (serviceName in registerServiceNames) {
            val list = try {
                api.listNamespacedService(namespace, null, null, null, null, serviceName, null, null, null, null)
            } catch (e: ApiException) {
                logger.error("ResponseBody is ${e.responseBody}")
                throw e
            }
            if (list.items.isEmpty()) {
                continue
            }
            val k8sService = list.items[0]
            val labelValue = k8sService.metadata?.labels?.get(serviceName) ?: continue
            val port = k8sService.spec?.ports?.first {
                it.name == labelValue || it.port == labelValue.toInt()
            }?.port ?: continue
            val host = k8sService.metadata?.name ?: continue
            logger.info("Grpc channel is $host:$port.")
            val channel = createGrpcChannel(host, port)
            val service = ProtoTypes.getRegisterService(serviceName)
                    ?: throw IllegalStateException("Grpc service not be found.")
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
