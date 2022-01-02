package com.bybutter.sisyphus.middleware.grpc.proxy

import com.bybutter.sisyphus.middleware.grpc.ChannelBuilderInterceptor
import com.bybutter.sisyphus.middleware.grpc.ClientBuilderInterceptor
import com.bybutter.sisyphus.middleware.grpc.ClientRegistrar
import com.bybutter.sisyphus.middleware.grpc.ClientRepository
import com.bybutter.sisyphus.middleware.grpc.ManagedChannelLifecycle
import com.bybutter.sisyphus.rpc.CallOptionsInterceptor
import com.bybutter.sisyphus.spring.BeanUtils
import io.grpc.ClientInterceptor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ProxyClientRepository : ClientRepository {

    override var order: Int = Int.MAX_VALUE - 1000

    override fun listClientBeanDefinition(
        beanFactory: ConfigurableListableBeanFactory,
        environment: Environment
    ): List<AbstractBeanDefinition> {
        val grpcClientProxies = BeanUtils.getSortedBeans(beanFactory, GrpcClientProxy::class.java)

        if (grpcClientProxies.values.isEmpty()) return arrayListOf()
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        val channelBuilderInterceptors = BeanUtils.getSortedBeans(beanFactory, ChannelBuilderInterceptor::class.java)
        val builderInterceptors = BeanUtils.getSortedBeans(beanFactory, ClientBuilderInterceptor::class.java)
        val clientInterceptors = BeanUtils.getSortedBeans(beanFactory, ClientInterceptor::class.java)
        val optionsInterceptors = BeanUtils.getSortedBeans(beanFactory, CallOptionsInterceptor::class.java)
        val managedChannelLifecycle =
            beanFactory.getBean<ManagedChannelLifecycle>(ClientRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_CHANNEL_LIFECYCLE)

        for (grpcClientProxy in grpcClientProxies.values) {
            val channel = createGrpcChannel(grpcClientProxy.target, channelBuilderInterceptors.values, managedChannelLifecycle)
            beanFactory.registerSingleton(grpcClientProxy.name, channel)
            for (service in grpcClientProxy.services) {
                val client = getClientFromService(service)
                val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(client as Class<Any>) {
                    interceptStub(
                        createGrpcClient(client, channel, optionsInterceptors.values, grpcClientProxy.options),
                        builderInterceptors.values,
                        clientInterceptors.values
                    )
                }
                beanDefinitionList.add(clientBeanDefinition.beanDefinition)
            }
        }
        return beanDefinitionList
    }
}
