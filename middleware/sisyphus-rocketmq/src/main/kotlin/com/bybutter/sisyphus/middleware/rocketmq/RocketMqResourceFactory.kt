package com.bybutter.sisyphus.middleware.rocketmq

import org.apache.rocketmq.client.consumer.MQConsumer
import org.apache.rocketmq.client.producer.MQProducer

interface RocketMqResourceFactory {
    fun createProducer(producerProperty: RocketMqProducerProperty): MQProducer

    fun createConsumer(
        consumerProperty: RocketMqConsumerProperty,
        metadata: MessageConsumer,
        listener: MessageListener<*>,
        loggers: List<RocketMqLogger>
    ): MQConsumer
}
