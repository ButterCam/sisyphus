package com.bybutter.sisyphus.middleware.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.support.AutowireCandidateQualifier
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class RedisConnectionRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanFactory = registry as ConfigurableListableBeanFactory

        val properties = beanFactory.getBeansOfType<RedisProperty>().toMutableMap()
        val elasticProperties = Binder.get(environment)
                .bind("sisyphus", RedisProperties::class.java)
                .orElse(null)?.redis ?: mapOf()

        properties += elasticProperties

        if (properties.isEmpty()) return

        for ((name, property) in properties) {
            val beanName = "$CONNECTION_PREFIX:$name"
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(StatefulRedisConnection::class.java) {
                val redisClient = beanFactory.getBean(RedisClientFactory::class.java).createClient(property)
                redisClient.connect()
            }.setDestroyMethodName("close").beanDefinition
            beanDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(beanName, beanDefinition)

            val clientBeanName = "$CLIENT_PREFIX:$name"
            val clientBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RedisClient::class.java) {
                beanFactory.getBean(RedisClientFactory::class.java).createClient(property)
            }.beanDefinition
            clientBeanDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(clientBeanName, clientBeanDefinition)
        }
    }

    companion object {
        private const val CONNECTION_PREFIX = "sisyphus:redis:connection"
        private const val CLIENT_PREFIX = "sisyphus:redis:client"
    }
}
