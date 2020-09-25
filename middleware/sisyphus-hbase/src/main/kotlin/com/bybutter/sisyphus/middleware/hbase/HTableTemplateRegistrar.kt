package com.bybutter.sisyphus.middleware.hbase

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
class HTableTemplateRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanFactory = registry as ConfigurableListableBeanFactory

        val properties = beanFactory.getBeansOfType<HBaseTableProperty>().toMutableMap()
        val hbaseProperties = Binder.get(environment)
                .bind("sisyphus", HBaseTableProperties::class.java)
                .orElse(null)?.hbase ?: mapOf()

        properties += hbaseProperties

        if (properties.isEmpty()) return

        for ((name, property) in properties) {
            val beanName = "$BEAN_NAME_PREFIX:$name"
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(HTableTemplate::class.java) {
                val factory = beanFactory.getBean(HBaseTemplateFactory::class.java)
                factory.createTemplate(property)
            }.beanDefinition
            beanDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(beanName, beanDefinition)
        }
    }

    companion object {
        private const val BEAN_NAME_PREFIX = "sisyphus:hbase"
    }
}
