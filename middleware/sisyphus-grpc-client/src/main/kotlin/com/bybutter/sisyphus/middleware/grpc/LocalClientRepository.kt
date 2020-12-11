package com.bybutter.sisyphus.middleware.grpc

import com.bybutter.sisyphus.rpc.CallOptionsInterceptor
import com.bybutter.sisyphus.rpc.GrpcServerConstants
import com.bybutter.sisyphus.rpc.RpcService
import com.bybutter.sisyphus.spring.BeanUtils
import io.grpc.CallOptions
import io.grpc.ClientInterceptor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.env.Environment

class LocalClientRepository : ClientRepository {
    override var order: Int = Int.MIN_VALUE + 1000

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition> {
        val localPort = environment.getProperty(GrpcServerConstants.GRPC_PORT_PROPERTY, Int::class.java, GrpcServerConstants.DEFAULT_GRPC_PORT)

        val channelBuilderInterceptors = BeanUtils.getSortedBeans(beanFactory, ChannelBuilderInterceptor::class.java)
        val builderInterceptors = BeanUtils.getSortedBeans(beanFactory, ClientBuilderInterceptor::class.java)
        val clientInterceptors = BeanUtils.getSortedBeans(beanFactory, ClientInterceptor::class.java)
        val optionsInterceptors = BeanUtils.getSortedBeans(beanFactory, CallOptionsInterceptor::class.java)
        val managedChannelLifecycle = beanFactory.getBean<ManagedChannelLifecycle>(ClientRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_CHANNEL_LIFECYCLE)

        val localChannel = createGrpcChannel("localhost:$localPort", channelBuilderInterceptors.values, managedChannelLifecycle)
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        for (serviceName in beanFactory.getBeanNamesForAnnotation(RpcServiceImpl::class.java)) {
            val serviceBeanDefinition = beanFactory.getBeanDefinition(serviceName)
            val serviceClass = Class.forName(serviceBeanDefinition.beanClassName)
            val rpcService = AnnotationUtils.findAnnotation(serviceClass, RpcService::class.java) ?: continue
            val service = rpcService.client.java.declaringClass
            val stub = getStubFromService(service)
            val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(rpcService.client.java as Class<Any>) {
                interceptStub(createGrpcClient(stub, localChannel, optionsInterceptors.values, CallOptions.DEFAULT), builderInterceptors.values, clientInterceptors.values)
            }
            beanDefinitionList.add(clientBeanDefinition.beanDefinition)
        }
        return beanDefinitionList
    }
}
