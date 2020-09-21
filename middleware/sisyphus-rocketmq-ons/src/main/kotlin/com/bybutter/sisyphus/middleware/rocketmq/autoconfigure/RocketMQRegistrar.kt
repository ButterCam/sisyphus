package com.bybutter.sisyphus.middleware.rocketmq.autoconfigure

import com.aliyun.openservices.ons.api.Consumer
import com.aliyun.openservices.ons.api.Producer
import com.aliyun.openservices.ons.api.PullConsumer
import com.aliyun.openservices.ons.api.batch.BatchConsumer
import com.aliyun.openservices.ons.api.order.OrderConsumer
import com.aliyun.openservices.ons.api.order.OrderProducer
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker
import com.aliyun.openservices.ons.api.transaction.TransactionProducer
import com.bybutter.sisyphus.middleware.rocketmq.ConsumerType
import com.bybutter.sisyphus.middleware.rocketmq.MessageQueueProperties
import com.bybutter.sisyphus.middleware.rocketmq.ProducerType
import com.bybutter.sisyphus.middleware.rocketmq.RocketMQProperty
import com.bybutter.sisyphus.middleware.rocketmq.RocketTemplateFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.AutowireCandidateQualifier
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class RocketMQRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanFactory = registry as ConfigurableListableBeanFactory

        val properties = beanFactory.getBeansOfType<RocketMQProperty>().toMutableMap()
        val amqpProperties = Binder.get(environment)
                .bind("sisyphus", MessageQueueProperties::class.java)
                .orElse(null)?.rocketmq ?: mapOf()

        properties += amqpProperties

        if (properties.isEmpty()) return

        for ((name, property) in properties) {
            when (property.producer.type) {
                ProducerType.NORMAL -> {
                    val producerName = "$BEAN_NAME_PREFIX:${name}Producer"
                    val producerDefinition = BeanDefinitionBuilder.genericBeanDefinition(Producer::class.java) {
                        val factory = beanFactory.getBean(RocketTemplateFactory::class.java)
                        factory.createProducer(property)
                    }.beanDefinition
                    producerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
                    producerDefinition.initMethodName = INIT_METHOD
                    producerDefinition.destroyMethodName = DESTROY_METHOD
                    registry.registerBeanDefinition(producerName, producerDefinition)
                }
                ProducerType.ORDER -> {
                    val orderProducerName = "$BEAN_NAME_PREFIX:${name}OrderProducer"
                    val orderProducerDefinition = BeanDefinitionBuilder.genericBeanDefinition(OrderProducer::class.java) {
                        val factory = beanFactory.getBean(RocketTemplateFactory::class.java)
                        factory.createOrderProducer(property)
                    }.beanDefinition
                    orderProducerDefinition.initMethodName = INIT_METHOD
                    orderProducerDefinition.destroyMethodName = DESTROY_METHOD
                    orderProducerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
                    registry.registerBeanDefinition(orderProducerName, orderProducerDefinition)
                }
                ProducerType.TRANSACTION -> {
                    val checkers = beanFactory.getBeanNamesForType(LocalTransactionChecker::class.java)
                    checkers.forEach {
                        val beanDefinition = beanFactory.getBeanDefinition(it) as AbstractBeanDefinition
                        val qualifiers = beanDefinition.qualifiers
                        qualifiers.forEach { qualifier ->
                            if (qualifier.typeName == property.qualifier.typeName) {
                                val transactionProducerName = "$BEAN_NAME_PREFIX:${name}TransactionProducer"
                                val transactionProducerDefinition = BeanDefinitionBuilder.genericBeanDefinition(TransactionProducer::class.java) {
                                    val factory = beanFactory.getBean(RocketTemplateFactory::class.java)
                                    val checker = beanFactory.getBean(it) as LocalTransactionChecker
                                    factory.createTransactionProducer(property, checker)
                                }.beanDefinition
                                transactionProducerDefinition.initMethodName = INIT_METHOD
                                transactionProducerDefinition.destroyMethodName = DESTROY_METHOD
                                transactionProducerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
                                registry.registerBeanDefinition(transactionProducerName, transactionProducerDefinition)
                            }
                        }
                    }
                }
            }
            when (property.consumer.type) {
                ConsumerType.NORMAL -> {
                    val pushConsumerName = "$BEAN_NAME_PREFIX:${name}Consumer"
                    val pushConsumerDefinition = BeanDefinitionBuilder.genericBeanDefinition(Consumer::class.java) {
                        val factory = beanFactory.getBean(RocketTemplateFactory::class.java)
                        factory.createConsumer(property)
                    }.beanDefinition
                    pushConsumerDefinition.initMethodName = INIT_METHOD
                    pushConsumerDefinition.destroyMethodName = DESTROY_METHOD
                    pushConsumerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
                    registry.registerBeanDefinition(pushConsumerName, pushConsumerDefinition)
                }
                ConsumerType.BATCH -> {
                    val batchConsumerName = "$BEAN_NAME_PREFIX:${name}BatchConsumer"
                    val batchConsumerDefinition = BeanDefinitionBuilder.genericBeanDefinition(BatchConsumer::class.java) {
                        val factory = beanFactory.getBean(RocketTemplateFactory::class.java)
                        factory.createBatchConsumer(property)
                    }.beanDefinition
                    batchConsumerDefinition.initMethodName = INIT_METHOD
                    batchConsumerDefinition.destroyMethodName = DESTROY_METHOD
                    batchConsumerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
                    registry.registerBeanDefinition(batchConsumerName, batchConsumerDefinition)
                }
                ConsumerType.ORDER -> {
                    val orderConsumerName = "$BEAN_NAME_PREFIX:${name}OrderConsumer"
                    val orderConsumerDefinition = BeanDefinitionBuilder.genericBeanDefinition(OrderConsumer::class.java) {
                        val factory = beanFactory.getBean(RocketTemplateFactory::class.java)
                        factory.createOrderedConsumer(property)
                    }.beanDefinition
                    orderConsumerDefinition.initMethodName = INIT_METHOD
                    orderConsumerDefinition.destroyMethodName = DESTROY_METHOD
                    orderConsumerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
                    registry.registerBeanDefinition(orderConsumerName, orderConsumerDefinition)
                }
                ConsumerType.PULL -> {
                    val pullConsumerName = "$BEAN_NAME_PREFIX:${name}PullConsumer"
                    val pullConsumerDefinition = BeanDefinitionBuilder.genericBeanDefinition(PullConsumer::class.java) {
                        val factory = beanFactory.getBean(RocketTemplateFactory::class.java)
                        factory.createPullConsumer(property)
                    }.beanDefinition
                    pullConsumerDefinition.initMethodName = INIT_METHOD
                    pullConsumerDefinition.destroyMethodName = DESTROY_METHOD
                    pullConsumerDefinition.addQualifier(AutowireCandidateQualifier(property.qualifier))
                    registry.registerBeanDefinition(pullConsumerName, pullConsumerDefinition)
                }
            }

            val messageQueuePropertyName = "${name}RocketMQProperty"
            val messageQueueProperty = BeanDefinitionBuilder.genericBeanDefinition(RocketMQProperty::class.java) {
                property
            }.beanDefinition
            messageQueueProperty.addQualifier(AutowireCandidateQualifier(property.qualifier))
            registry.registerBeanDefinition(messageQueuePropertyName, messageQueueProperty)
        }
    }

    companion object {
        private const val BEAN_NAME_PREFIX = "sisyphus:rocketmq"
        private const val INIT_METHOD = "start"
        private const val DESTROY_METHOD = "shutdown"
    }
}
