package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.DSLContext
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
class DslContextRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanFactory = registry as ConfigurableListableBeanFactory

        val properties = beanFactory.getBeansOfType<JdbcDatabaseProperty>().toMutableMap()
        val jdbcProperties = Binder.get(environment)
                .bind("sisyphus", JdbcDatabaseProperties::class.java)
                .orElse(null)?.jdbc ?: mapOf()

        properties += jdbcProperties

        if (properties.isEmpty()) return

        for ((name, property) in properties) {
            val beanName = "$BEAN_NAME_PREFIX:$name"
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(DSLContext::class.java) {
                val factory = beanFactory.getBean(DslContextFactory::class.java)
                factory.createContext(beanName, property)
            }.beanDefinition
            beanDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(beanName, beanDefinition)
        }
    }

    companion object {
        private const val BEAN_NAME_PREFIX = "sisyphus:jdbc"
    }
}
