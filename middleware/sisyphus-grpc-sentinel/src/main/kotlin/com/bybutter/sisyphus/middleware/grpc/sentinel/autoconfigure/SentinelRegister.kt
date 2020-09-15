package com.bybutter.sisyphus.middleware.grpc.sentinel.autoconfigure

import com.bybutter.sisyphus.middleware.grpc.sentinel.FileDataSourceInit
import com.bybutter.sisyphus.middleware.grpc.sentinel.RedisDataSourceInit
import com.bybutter.sisyphus.middleware.grpc.sentinel.SentinelDatabase
import com.bybutter.sisyphus.middleware.grpc.sentinel.SentinelProperties
import com.bybutter.sisyphus.middleware.grpc.sentinel.SentinelTemplateFactory
import com.bybutter.sisyphus.middleware.grpc.sentinel.SisyphusSentinelGrpcServerInterceptor
import com.bybutter.sisyphus.rpc.Code
import com.bybutter.sisyphus.rpc.StatusException
import io.lettuce.core.RedisClient
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SentinelRegister : BeanDefinitionRegistryPostProcessor, EnvironmentAware{
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

            System.setProperty("csp.sentinel.dashboard.server", property.serverAddr)
            System.setProperty("project.name", property.projectName)

        when(property.database){
            SentinelDatabase.NONE -> {

            }
            SentinelDatabase.REDIS -> {
                try {
                    if(property.redisClientName == null){
                        throw StatusException(Code.UNAVAILABLE, "redisClientName can not be null.")
                    }
                    val redisClient = beanFactory.getBean(property.redisClientName, RedisClient::class.java)
                    val beanName = "redisDataSourceInit"
                    val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RedisDataSourceInit::class.java) {
                        RedisDataSourceInit(redisClient, property)
                    }.beanDefinition
                    beanDefinition.initMethodName = "init"
                    registry.registerBeanDefinition(beanName, beanDefinition)
                }catch (e: ClassNotFoundException){
                    throw StatusException(Code.NOT_FOUND, "RedisClient bean not found.")
                }
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
