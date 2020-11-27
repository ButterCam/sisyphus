package com.bybutter.sisyphus.middleware.amqp

import com.bybutter.sisyphus.jackson.Json
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter

open class DefaultAmqpTemplateFactory : AmqpTemplateFactory {
    private val connectionFactories: MutableMap<String, ConnectionFactory> = hashMapOf()

    override fun createTemplate(property: MessageQueueProperty): AmqpTemplate {
        return RabbitTemplate(
                createConnectionFactory(property.host, property.port, property)
        ).apply {
            property.queue?.let {
                this.setDefaultReceiveQueue(it)
            }
            this.exchange = property.exchange ?: ""
            this.messageConverter = Jackson2JsonMessageConverter(Json.mapper)
        }
    }

    protected open fun createConnectionFactory(host: String, port: Int, property: MessageQueueProperty): ConnectionFactory {
        return connectionFactories.getOrPut("$host:$port/${property.vhost}") {
            CachingConnectionFactory(host, port).apply {
                this.virtualHost = property.vhost
                this.username = property.userName
                this.setPassword(property.password)
                property.confirmType?.let {
                    this.setPublisherConfirmType(it)
                }
            }
        }
    }
}
