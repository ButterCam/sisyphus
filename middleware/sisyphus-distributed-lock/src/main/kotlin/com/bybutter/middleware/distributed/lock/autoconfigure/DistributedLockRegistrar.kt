package com.bybutter.middleware.distributed.lock.autoconfigure

import com.bybutter.middleware.distributed.lock.RedisLockProperty
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class DistributedLockRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val properties: RedisLockProperty? = try {
            Binder.get(environment)
                    .bindOrCreate("sisyphus.redisLock", RedisLockProperty::class.java)
        } catch (e: Exception) {
            null
        }

        if (properties != null) {
            val propertyName = "RedisLockProperty"
            val propertyDefinition = BeanDefinitionBuilder.genericBeanDefinition(RedisLockProperty::class.java) {
                properties
            }.beanDefinition
            registry.registerBeanDefinition(propertyName, propertyDefinition)
        }
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }
}
