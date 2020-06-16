package com.bybutter.sisyphus.middleware.grpc.client.kubernetes

import com.bybutter.sisyphus.middleware.grpc.ClientRepository
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.rpc.RpcService
import io.grpc.Channel
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.Config
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.full.companionObject
@Component
class KubernetesClientRepository : ClientRepository {

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory): List<AbstractBeanDefinition> {
        val client = Config.fromCluster()
        val api = CoreV1Api(client)
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
            val stub = service.kotlin.companionObject?.java?.classes?.firstOrNull {
                it.simpleName == "Stub"
            } ?: throw IllegalStateException("Grpc service must have stub class in companion.")
            val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(service as Class<Any>)  {
                processStub(createGrpcClient(stub, channel), beanFactory)
            }
            beanDefinitionList.add(clientBeanDefinition.beanDefinition)
        }
        return beanDefinitionList
    }
}