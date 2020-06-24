package com.bybutter.sisyphus.middleware.grpc

import com.bybutter.sisyphus.spi.ServiceLoader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ClientRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

        val clientRepositories = ServiceLoader.load(ClientRepository::class.java).sortedBy { it.order }

        val registry = beanFactory as BeanDefinitionRegistry

        for (clientRepository in clientRepositories) {
            val clientBeanList = clientRepository.listClientBeanDefinition(beanFactory, environment)
            for (clientBean in clientBeanList) {
                val beanName = clientBean.beanClass.name
                if (!registry.containsBeanDefinition(beanName)) {
                    registry.registerBeanDefinition(beanName, clientBean)
                    logger.info("Register '$beanName Client' via '${clientRepository.javaClass.simpleName}'.")
                }
            }
        }
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
    }
    companion object {
        private val logger = LoggerFactory.getLogger(ClientRegistrar::class.java)
    }
}
