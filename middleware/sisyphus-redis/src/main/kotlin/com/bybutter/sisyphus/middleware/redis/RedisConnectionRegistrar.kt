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
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
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
            val beanName = "$BEAN_NAME_PREFIX:$name"
            val connectionFactoryBeanName = "$CONNECTION_FACTORY_BEAN_NAME_PREFIX:$name"

            val redisConnectionFactory = LettuceConnectionFactory(convertToConfig(property))
            val redisConnectionFactoryBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RedisConnectionFactory::class.java) {
                redisConnectionFactory
            }.setDestroyMethodName("destroy").beanDefinition
            redisConnectionFactoryBeanDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(connectionFactoryBeanName, redisConnectionFactoryBeanDefinition)

            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(StatefulRedisConnection::class.java) {
                RedisClient.create(redisConnectionFactory.clientResources).connect()
            }.setDestroyMethodName("close").beanDefinition
            beanDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(beanName, beanDefinition)
        }
    }

    private fun convertToConfig(property: RedisProperty): RedisStandaloneConfiguration {
        return RedisStandaloneConfiguration(property.host, property.port).apply {
            this.password = RedisPassword.of(property.password)
            this.database = property.database ?: 0
        }
    }

    companion object {
        private const val BEAN_NAME_PREFIX = "sisyphus:redis"
        private const val CONNECTION_FACTORY_BEAN_NAME_PREFIX = "sisyphus:redis:connection:factory"
    }
}
