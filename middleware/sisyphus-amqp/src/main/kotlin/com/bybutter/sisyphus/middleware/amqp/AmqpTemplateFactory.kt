package com.bybutter.sisyphus.middleware.amqp

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.ConnectionFactory

interface AmqpTemplateFactory {
    fun createTemplate(property: MessageQueueProperty): AmqpTemplate
}
