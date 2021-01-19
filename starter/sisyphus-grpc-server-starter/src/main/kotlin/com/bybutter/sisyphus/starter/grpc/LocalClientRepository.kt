package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.middleware.grpc.ChannelBuilderInterceptor
import com.bybutter.sisyphus.middleware.grpc.ClientBuilderInterceptor
import com.bybutter.sisyphus.middleware.grpc.ClientRegistrar
import com.bybutter.sisyphus.middleware.grpc.ClientRepository
import com.bybutter.sisyphus.middleware.grpc.ManagedChannelLifecycle
import com.bybutter.sisyphus.middleware.grpc.RpcServiceImpl
import com.bybutter.sisyphus.rpc.CallOptionsInterceptor
import com.bybutter.sisyphus.spring.BeanUtils
import io.grpc.CallOptions
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class LocalClientRepository : ClientRepository {

    @Autowired
    private lateinit var serviceConfig: ServiceConfig

    override var order: Int = Int.MIN_VALUE + 1000

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition> {

        val builderInterceptors = BeanUtils.getSortedBeans(beanFactory, ClientBuilderInterceptor::class.java)
        val clientInterceptors = BeanUtils.getSortedBeans(beanFactory, ClientInterceptor::class.java)
        val optionsInterceptors = BeanUtils.getSortedBeans(beanFactory, CallOptionsInterceptor::class.java)
        val managedChannelLifecycle = beanFactory.getBean<ManagedChannelLifecycle>(ClientRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_CHANNEL_LIFECYCLE)

        val localChannel = serviceConfig.localChannel.apply {
            managedChannelLifecycle.registerManagedChannel(this as ManagedChannel)
        }
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        for (serviceName in beanFactory.getBeanNamesForAnnotation(RpcServiceImpl::class.java)) {
            val serviceBeanDefinition = beanFactory.getBeanDefinition(serviceName)
            val serviceClass = Class.forName(serviceBeanDefinition.beanClassName)
            val stub = getClientFromService(serviceClass.superclass)
            val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(stub as Class<Any>) {
                interceptStub(createGrpcClient(stub, localChannel, optionsInterceptors.values, CallOptions.DEFAULT), builderInterceptors.values, clientInterceptors.values)
            }
            beanDefinitionList.add(clientBeanDefinition.beanDefinition)
        } 
        return beanDefinitionList
    }
}
