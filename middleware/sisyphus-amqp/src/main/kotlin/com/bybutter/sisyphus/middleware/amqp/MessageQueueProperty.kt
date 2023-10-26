package com.bybutter.sisyphus.middleware.amqp

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.boot.context.properties.NestedConfigurationProperty

data class MessageQueueProperty(
    val qualifier: Class<*>,
    val host: String,
    val port: Int,
    val userName: String,
    val password: String,
    val vhost: String,
    val exchange: String?,
    val queue: String?,
    val confirmType: CachingConnectionFactory.ConfirmType?,
    val extensions: Map<String, Any> = mapOf(),
)

data class MessageQueueProperties(
    @NestedConfigurationProperty
    val amqp: Map<String, MessageQueueProperty>,
)
