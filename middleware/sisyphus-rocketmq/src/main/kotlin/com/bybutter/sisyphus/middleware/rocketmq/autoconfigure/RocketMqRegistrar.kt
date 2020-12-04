package com.bybutter.sisyphus.middleware.rocketmq.autoconfigure

import com.bybutter.sisyphus.middleware.rocketmq.MessageConsumer
import com.bybutter.sisyphus.middleware.rocketmq.MessageListener
import com.bybutter.sisyphus.middleware.rocketmq.RocketMqConsumerProperty
import com.bybutter.sisyphus.middleware.rocketmq.RocketMqProducerProperty
import com.bybutter.sisyphus.middleware.rocketmq.RocketMqProperties
import com.bybutter.sisyphus.middleware.rocketmq.RocketMqResourceFactory
import org.apache.rocketmq.client.consumer.MQConsumer
import org.apache.rocketmq.client.producer.MQProducer
import org.slf4j.LoggerFactory
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
class RocketMqRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanFactory = registry as ConfigurableListableBeanFactory

        val rocketMqProperties = Binder.get(environment)
            .bind("sisyphus.rocketmq", RocketMqProperties::class.java)
            .orElse(null)
        val producerProperties = (rocketMqProperties?.producers
            ?: mapOf()) + beanFactory.getBeansOfType<RocketMqProducerProperty>()
        val consumerProperties = (rocketMqProperties?.consumers
            ?: mapOf()) + beanFactory.getBeansOfType<RocketMqConsumerProperty>()

        for ((name, property) in producerProperties) {
            val producerName = "$BEAN_NAME_PREFIX:${name}Producer"
            val producerDefinition = BeanDefinitionBuilder.genericBeanDefinition(MQProducer::class.java) {
                val factory = beanFactory.getBean(RocketMqResourceFactory::class.java)
                factory.createProducer(property)
            }.beanDefinition
            producerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            producerDefinition.initMethodName = INIT_METHOD
            producerDefinition.destroyMethodName = DESTROY_METHOD
            registry.registerBeanDefinition(producerName, producerDefinition)
        }

        val consumers = consumerProperties.values.associateBy { it.qualifier.name }
        val listeners = beanFactory.getBeanNamesForType(MessageListener::class.java)
        for (listener in listeners) {
            val definition = beanFactory.getBeanDefinition(listener)
            if (definition !is AnnotatedBeanDefinition) continue
            val annotation = definition.metadata.annotations[MessageConsumer::class.java].synthesize()

            val selectedConsumers = definition.metadata.annotationTypes.mapNotNull {
                consumers[it]
            }
            if (selectedConsumers.isEmpty()) {
                logger.warn("Listener '$listener(${definition.beanClassName})' don't has been annotated with consumer qualifiers, skip it.")
                continue
            }
            if (selectedConsumers.size > 2) {
                throw IllegalStateException("Listener '$listener(${definition.beanClassName})' has multi consumer qualifiers [${selectedConsumers.joinToString { it.qualifier.name }}]")
            }
            val consumer = selectedConsumers.first()

            val consumerName = "$BEAN_NAME_PREFIX:${listener}Consumer"
            val consumerDefinition = BeanDefinitionBuilder.genericBeanDefinition(MQConsumer::class.java) {
                val factory = beanFactory.getBean(RocketMqResourceFactory::class.java)
                factory.createConsumer(consumer, annotation, beanFactory.getBean<MessageListener<*>>(listener)).also {
                    logger.info("RocketMQ listener (${annotation.groupId}) registered on topic '${annotation.topic}(${annotation.filter})'.")
                }
            }.beanDefinition
            consumerDefinition.addQualifier(AutowireCandidateQualifier(consumer.qualifier))
            consumerDefinition.initMethodName = INIT_METHOD
            consumerDefinition.destroyMethodName = DESTROY_METHOD
            registry.registerBeanDefinition(consumerName, consumerDefinition)
        }
    }

    companion object {
        private const val BEAN_NAME_PREFIX = "sisyphus:rocketmq"
        private const val INIT_METHOD = "start"
        private const val DESTROY_METHOD = "shutdown"
        private val logger = LoggerFactory.getLogger(RocketMqRegistrar::class.java)
    }
}
