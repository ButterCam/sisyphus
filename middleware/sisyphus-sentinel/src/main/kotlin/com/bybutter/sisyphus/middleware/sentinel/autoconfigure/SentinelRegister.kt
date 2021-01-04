package com.bybutter.sisyphus.middleware.sentinel.autoconfigure

import com.bybutter.sisyphus.middleware.redis.RedisClientFactory
import com.bybutter.sisyphus.middleware.redis.RedisProperties
import com.bybutter.sisyphus.middleware.redis.RedisProperty
import com.bybutter.sisyphus.middleware.sentinel.SentinelDatabase
import com.bybutter.sisyphus.middleware.sentinel.SentinelProperties
import com.bybutter.sisyphus.middleware.sentinel.SentinelTemplateFactory
import com.bybutter.sisyphus.middleware.sentinel.interceptor.SisyphusSentinelGrpcServerInterceptor
import com.bybutter.sisyphus.middleware.sentinel.persistence.FileDataSourceInit
import com.bybutter.sisyphus.middleware.sentinel.persistence.RedisDataSourceInit
import com.bybutter.sisyphus.rpc.Code
import com.bybutter.sisyphus.rpc.StatusException
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
                .bind("sisyphus.sentinel", SentinelProperties::class.java).orElse(null) ?: return

        val redisProperties = Binder.get(environment)
                .bind("sisyphus", RedisProperties::class.java)
                .orElse(null)?.redis ?: mapOf()
        var redisClientProperty: RedisProperty? = null
        redisProperties.forEach { (_, redisProperty) ->
            if (redisProperty.qualifier == property.redisQualifier)
                redisClientProperty = redisProperty
        }

        System.setProperty("csp.sentinel.dashboard.server", property.dashboardAddr)
        System.setProperty("project.name", property.projectName)

        when (property.database) {
            SentinelDatabase.NONE -> {
            }
            SentinelDatabase.REDIS -> {
                val beanName = "redisDataSourceInit"
                val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RedisDataSourceInit::class.java) {
                    val redisClient = redisClientProperty?.let { beanFactory.getBean(RedisClientFactory::class.java).createClient(it) }
                            ?: throw StatusException(Code.NOT_FOUND, "redisClient is not found.")
                    RedisDataSourceInit(redisClient, property)
                }.beanDefinition
                beanDefinition.initMethodName = "init"
                registry.registerBeanDefinition(beanName, beanDefinition)
            }
            SentinelDatabase.FILE -> {
                val beanName = "FileDataSourceInit"
                val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(FileDataSourceInit::class.java) {
                    FileDataSourceInit(property)
                }.beanDefinition
                beanDefinition.initMethodName = "init"
                registry.registerBeanDefinition(beanName, beanDefinition)
            }
        }

        val beanName = "SentinelGrpcServerInterceptor"
        val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(SisyphusSentinelGrpcServerInterceptor::class.java) {
            val factory = beanFactory.getBean(SentinelTemplateFactory::class.java)
            factory.createSentinelGrpcServerInterceptor(property.fallbackMessage)
        }.beanDefinition
        registry.registerBeanDefinition(beanName, beanDefinition)

        val sentinelProperties = "SentinelProperties"
        val sentinelPropertiesDefinition = BeanDefinitionBuilder.genericBeanDefinition(SentinelProperties::class.java) {
            property
        }.beanDefinition
        registry.registerBeanDefinition(sentinelProperties, sentinelPropertiesDefinition)
    }
}
