package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.bybutter.sisyphus.starter.grpc.SisyphusSentinelGrpcServerInterceptor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SentinelRegister : BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanFactory = registry as ConfigurableListableBeanFactory

        val property = Binder.get(environment)
                .bind("sisyphus.sentinel", SentinelProperties::class.java) ?: return

            System.setProperty("csp.sentinel.dashboard.server", property.get().serverAddr)
            System.setProperty("project.name", property.get().projectName)

        val beanName = "SentinelGrpcServerInterceptor"
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(SisyphusSentinelGrpcServerInterceptor::class.java) {
                val factory = beanFactory.getBean(SentinelTemplateFactory::class.java)
                factory.createSentinelGrpcServerInterceptor(property.get().fallbackMessage)
            }.beanDefinition
            registry.registerBeanDefinition(beanName, beanDefinition)

        val sentinelProperties = "SentinelProperties"
        val sentinelPropertiesDefinition = BeanDefinitionBuilder.genericBeanDefinition(SentinelProperties::class.java) {
            property.get()
        }.beanDefinition
        registry.registerBeanDefinition(sentinelProperties, sentinelPropertiesDefinition)
    }
}
