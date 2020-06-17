package com.bybutter.sisyphus.middleware.grpc

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ClientRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware, ApplicationContextAware {
    companion object {
        private val logger = LoggerFactory.getLogger(ClientRegistrar::class.java)
    }

    private lateinit var environment: Environment

    private lateinit var clientRepositories: List<ClientRepository>

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val registry = beanFactory as BeanDefinitionRegistry

        for (clientRepository in clientRepositories) {
            val clientBeanList = clientRepository.listClientBeanDefinition(beanFactory)
            for (clientBean in clientBeanList) {
                val beanName = clientBean.beanClass.name
                if (!registry.containsBeanDefinition(beanName)) {
                    registry.registerBeanDefinition(beanName, clientBean)
                }
            }
        }
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        clientRepositories = applicationContext.getBeansOfType<ClientRepository>().values.toList().sortedBy {
            it.order
        }
    }
}
