package com.bybutter.sisyphus.middleware.grpc.client.kubernetes

import com.bybutter.sisyphus.middleware.grpc.ClientRepository
import com.bybutter.sisyphus.protobuf.ProtoTypes
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.Config
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.env.Environment
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class KubernetesClientRepository : ClientRepository {

    override var order: Int = Int.MIN_VALUE + 2000

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
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        val registerServices = ProtoTypes.getProtoToServiceMap()
        for ((serviceName, service) in registerServices) {
            val list = api.listNamespacedService(namespace, null, null, null, null, serviceName, null, null, null, null)
            val channel = list.items[0].spec?.ports?.get(0)?.port?.let {
                createGrpcChannel(serviceName, it)
            } ?: continue
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
