package com.bybutter.sisyphus.middleware.grpc

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class RemoteClientRepository : ClientRepository {

    override var order: Int = Int.MIN_VALUE

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition> {
        val properties = beanFactory.getBeansOfType<GrpcChannelProperty>()
        if (properties.isEmpty()) return arrayListOf()
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        for (property in properties.values) {
            val channel = createGrpcChannel(property)
            beanFactory.registerSingleton(property.name, channel)
            for (service in property.services) {
                val client = getClientFromService(service)
                val stub = getStubFromService(service)
                val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(client as Class<Any>) {
                    processStub(createGrpcClient(stub, channel, property), beanFactory)
                }
                beanDefinitionList.add(clientBeanDefinition.beanDefinition)
            }
        }
        return beanDefinitionList
    }
}
