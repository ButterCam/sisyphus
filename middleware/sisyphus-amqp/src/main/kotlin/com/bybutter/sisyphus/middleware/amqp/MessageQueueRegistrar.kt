package com.bybutter.sisyphus.middleware.amqp

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.MessageListener
import org.springframework.amqp.rabbit.listener.MessageListenerContainer
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBean
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
class MessageQueueRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanFactory = registry as ConfigurableListableBeanFactory

        val properties = beanFactory.getBeansOfType<MessageQueueProperty>().toMutableMap()
        val amqpProperties = Binder.get(environment)
            .bind("sisyphus", MessageQueueProperties::class.java)
            .orElse(null)?.amqp ?: mapOf()

        properties += amqpProperties

        if (properties.isEmpty()) return

        val listeners = registry.getBeanNamesForType(MessageListener::class.java).mapNotNull {
            it to beanFactory.getBeanDefinition(it) as? AnnotatedBeanDefinition
        }

        for ((name, property) in properties) {
            val beanName = "$BEAN_NAME_PREFIX:$name"
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(AmqpTemplate::class.java) {
                val factory = beanFactory.getBean(AmqpTemplateFactory::class.java)
                factory.createTemplate(property)
            }.beanDefinition
            beanDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(beanName, beanDefinition)

            for ((listenerName, listenerDefinition) in listeners) {
                listenerDefinition ?: continue
                if (!listenerDefinition.metadata.annotationTypes.contains(property.qualifier.name)) continue

                val containerBeanName = "$listenerName:container"
                val containerBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(MessageListenerContainer::class.java) {
                    val factory = beanFactory.getBean(AmqpTemplateFactory::class.java)
                    val listener = beanFactory.getBean(listenerName) as MessageListener
                    factory.createListenerContainer(property).apply {
                        this.setMessageListener(listener)
                    }
                }.beanDefinition
                registry.registerBeanDefinition(containerBeanName, containerBeanDefinition)
            }
        }
    }

    companion object {
        private const val BEAN_NAME_PREFIX = "sisyphus:amqp"
    }
}
