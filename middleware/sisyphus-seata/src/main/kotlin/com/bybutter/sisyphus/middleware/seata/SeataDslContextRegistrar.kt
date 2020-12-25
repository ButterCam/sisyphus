package com.bybutter.sisyphus.middleware.seata

import com.bybutter.sisyphus.middleware.jdbc.DslContextFactory
import com.bybutter.sisyphus.middleware.jdbc.JdbcDatabaseProperties
import com.bybutter.sisyphus.middleware.jdbc.JdbcDatabaseProperty
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
import javax.sql.DataSource

@Component
class SeataDslContextRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
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
            val dataSourceName = "${name}DataSource"
            val dataSourceDefinition = BeanDefinitionBuilder.genericBeanDefinition(DataSource::class.java) {
                val factory = beanFactory.getBean(DslContextFactory::class.java)
                factory.createDatasource(property.qualifier, property)
            }.beanDefinition
            dataSourceDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(dataSourceName, dataSourceDefinition)
        }
    }
}
