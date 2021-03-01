package com.bybutter.sisyphus.middleware.amqp

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer

interface AmqpTemplateFactory {
    fun createTemplate(property: MessageQueueProperty): AmqpTemplate

    fun createListenerContainer(property: MessageQueueProperty): SimpleMessageListenerContainer
}
