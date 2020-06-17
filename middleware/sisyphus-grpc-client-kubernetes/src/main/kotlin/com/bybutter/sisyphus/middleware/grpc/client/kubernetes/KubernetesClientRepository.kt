package com.bybutter.sisyphus.middleware.grpc.client.kubernetes

import com.bybutter.sisyphus.middleware.grpc.ClientRepository
import com.bybutter.sisyphus.protobuf.ProtoTypes
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.Config
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
@Component
class KubernetesClientRepository : ClientRepository {

    override var order: Int = Int.MIN_VALUE + 1000

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition> {
        val api = CoreV1Api(Config.fromCluster())
        val namespace = String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace")), Charset.defaultCharset())
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        val registerServices = ProtoTypes.getRegisteredServices()
        for (registerService in registerServices) {
            val list = api.listNamespacedService(namespace, null, null, null, null, registerService, null, null, null, null)
            val channel = list.items[0].spec?.ports?.get(0)?.port?.let {
                createGrpcChannel(registerService, it)
            } ?: continue
            val service = ProtoTypes.getProtoToServiceMap(registerService)
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
