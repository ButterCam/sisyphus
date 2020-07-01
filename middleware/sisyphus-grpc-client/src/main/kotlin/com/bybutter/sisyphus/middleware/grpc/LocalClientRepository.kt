package com.bybutter.sisyphus.middleware.grpc

import com.bybutter.sisyphus.rpc.GrpcServerConstants
import com.bybutter.sisyphus.rpc.RpcService
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.env.Environment

class LocalClientRepository : ClientRepository {

    override var order: Int = Int.MIN_VALUE + 1000

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition> {
        val localPort = environment.getProperty(GrpcServerConstants.GRPC_PORT_PROPERTY, Int::class.java, GrpcServerConstants.DEFAULT_GRPC_PORT)
        val localChannel = createGrpcChannel("localhost", localPort)
        channelLifecycleManager(localChannel, beanFactory)
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        for (serviceName in beanFactory.getBeanNamesForAnnotation(RpcServiceImpl::class.java)) {
            val serviceBeanDefinition = beanFactory.getBeanDefinition(serviceName)
            val serviceClass = Class.forName(serviceBeanDefinition.beanClassName)
            val rpcService = AnnotationUtils.findAnnotation(serviceClass, RpcService::class.java) ?: continue
            val service = rpcService.client.java.declaringClass
            val stub = getStubFromService(service)
            val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(rpcService.client.java as Class<Any>) {
                processStub(createGrpcClient(stub, localChannel), beanFactory)
            }
            beanDefinitionList.add(clientBeanDefinition.beanDefinition)
        }
        return beanDefinitionList
    }
}
