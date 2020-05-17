package com.bybutter.sisyphus.middleware.amqp

import org.springframework.amqp.core.AmqpTemplate

interface AmqpTemplateFactory {
    fun createTemplate(property: MessageQueueProperty): AmqpTemplate
}
