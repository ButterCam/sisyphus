package com.bybutter.sisyphus.middleware.amqp

import com.bybutter.sisyphus.jackson.Json
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter

open class DefaultAmqpTemplateFactory : AmqpTemplateFactory {
    private val connectionFactories: MutableMap<String, ConnectionFactory> = hashMapOf()

    override fun createTemplate(property: MessageQueueProperty): AmqpTemplate {
        return RabbitTemplate(
                createConnectionFactory(property)
        ).apply {
            property.queue?.let {
                this.setDefaultReceiveQueue(it)
            }
            this.exchange = property.exchange ?: ""
            this.messageConverter = Jackson2JsonMessageConverter(Json.mapper)
        }
    }

    override fun createConnectionFactory(property: MessageQueueProperty): ConnectionFactory {
        return connectionFactories.getOrPut("${property.host}:${property.port}/${property.vhost}") {
            CachingConnectionFactory(property.host, property.port).apply {
                this.virtualHost = property.vhost
                this.username = property.userName
                this.setPassword(property.password)
            }
        }
    }
}
