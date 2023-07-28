package com.bybutter.sisyphus.middleware.kafka.autoconfigure

import com.bybutter.sisyphus.middleware.kafka.KafkaConsumer
import com.bybutter.sisyphus.middleware.kafka.KafkaConsumerLifecycle
import com.bybutter.sisyphus.middleware.kafka.KafkaConsumerProperty
import com.bybutter.sisyphus.middleware.kafka.KafkaListener
import com.bybutter.sisyphus.middleware.kafka.KafkaLogger
import com.bybutter.sisyphus.middleware.kafka.KafkaProducerProperty
import com.bybutter.sisyphus.middleware.kafka.KafkaProperties
import com.bybutter.sisyphus.middleware.kafka.KafkaResourceFactory
import com.bybutter.sisyphus.reflect.uncheckedCast
import com.bybutter.sisyphus.spring.BeanUtils
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.support.AutowireCandidateQualifier
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.context.SmartLifecycle
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class KafkaRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanFactory = registry as ConfigurableListableBeanFactory

        val kafkaProperties = Binder.get(environment)
            .bind("sisyphus.kafka", KafkaProperties::class.java)
            .orElse(null)
        val producerProperties = (
            kafkaProperties?.producers
                ?: mapOf()
            ) + beanFactory.getBeansOfType<KafkaProducerProperty>()
        val consumerProperties = (
            kafkaProperties?.consumers
                ?: mapOf()
            ) + beanFactory.getBeansOfType<KafkaConsumerProperty>()

        for ((name, property) in producerProperties) {
            val producerName = "$BEAN_NAME_PREFIX:${name}Producer"
            val producerDefinition = BeanDefinitionBuilder.genericBeanDefinition(KafkaProducer::class.java) {
                val factory = beanFactory.getBean(KafkaResourceFactory::class.java)
                factory.createProducer(property)
            }.beanDefinition
            producerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
            producerDefinition.destroyMethodName = CLOSE_METHOD
            registry.registerBeanDefinition(producerName, producerDefinition)
        }

        val consumers = consumerProperties.values.associateBy { it.qualifier.name }
        val listeners = beanFactory.getBeanNamesForType(KafkaListener::class.java)
        for (listener in listeners) {
            val definition = beanFactory.getBeanDefinition(listener)
            if (definition !is AnnotatedBeanDefinition) continue
            val annotation = definition.metadata.annotations[KafkaConsumer::class.java].synthesize()

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
            val consumerDefinition = BeanDefinitionBuilder.genericBeanDefinition(SmartLifecycle::class.java) {
                val addedLogger = mutableSetOf<String>()
                val loggers = BeanUtils.getSortedBeans(beanFactory, KafkaLogger::class.java).values.mapNotNull {
                    if (it.id.isNotEmpty() && addedLogger.contains(it.id)) return@mapNotNull null
                    addedLogger += it.id
                    it
                }
                val listener = beanFactory.getBean(listener) as KafkaListener<*, *>
                KafkaConsumerLifecycle(
                    beanFactory.getBean(KafkaResourceFactory::class.java)
                        .createConsumer(consumer, annotation, listener, loggers)
                        .also {
                            logger.info(
                                "Kafka listener (${it.groupMetadata().groupId()}) registered on topics '${
                                annotation.topics.joinToString()
                                    .takeIf { it.isNotEmpty() } ?: annotation.topicPattern
                                }'."
                            )
                        },
                    listener.uncheckedCast(),
                    loggers
                )
            }.beanDefinition
            consumerDefinition.addQualifier(AutowireCandidateQualifier(consumer.qualifier))
            registry.registerBeanDefinition(consumerName, consumerDefinition)
        }
    }

    companion object {
        private const val BEAN_NAME_PREFIX = "sisyphus:kafka"
        private const val CLOSE_METHOD = "close"
        private val logger = LoggerFactory.getLogger(KafkaRegistrar::class.java)
    }
}
