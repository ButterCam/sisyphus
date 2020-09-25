package com.bybutter.sisyphus.middleware.rocketmq

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class RocketMQProperty(
    val qualifier: Class<*>,
    val nameServerAddr: String,
    val accessKey: String,
    val secretKey: String,
    val groupId: String,
    val topic: String,
    val instanceId: String,
    val instanceName: String,
    val producer: Producer,
    val consumer: Consumer
)

data class MessageQueueProperties(
    @NestedConfigurationProperty
    val rocketmq: Map<String, RocketMQProperty>
)

data class Producer(
    val type: ProducerType
)
data class Consumer(
    val type: ConsumerType
)
enum class ProducerType {
    NORMAL,
    ORDER,
    TRANSACTION
}
enum class ConsumerType {
    NORMAL,
    BATCH,
    ORDER,
    PULL
}
