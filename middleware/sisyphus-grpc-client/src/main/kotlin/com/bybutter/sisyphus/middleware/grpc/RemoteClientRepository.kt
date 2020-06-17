package com.bybutter.sisyphus.middleware.grpc

import kotlin.reflect.full.companionObject
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.stereotype.Component

@Component
class RemoteClientRepository : ClientRepository {

    override var order: Int = Int.MIN_VALUE

    override fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory): List<AbstractBeanDefinition> {
        val properties = beanFactory.getBeansOfType<GrpcChannelProperty>()
        if (properties.isEmpty()) return arrayListOf()
        val beanDefinitionList = arrayListOf<AbstractBeanDefinition>()
        for (property in properties.values) {
            val channel = createGrpcChannel(property)
            beanFactory.registerSingleton(property.name, channel)
            for (service in property.services) {
                val client = service.declaredClasses.firstOrNull { it.simpleName == "Client" }
                        ?: throw IllegalStateException("Grpc service must have nested class named 'Client'.")
                val stub = service.kotlin.companionObject?.java?.classes?.firstOrNull {
                    it.simpleName == "Stub"
                } ?: throw IllegalStateException("Grpc service must have stub class in companion.")
                val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(client as Class<Any>) {
                    processStub(createGrpcClient(stub, channel, property), beanFactory)
                }
                beanDefinitionList.add(clientBeanDefinition.beanDefinition)
            }
        }
        return beanDefinitionList
    }
}
