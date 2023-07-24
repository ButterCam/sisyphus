package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

interface KafkaResourceFactory {
    fun createProducer(producerProperty: KafkaProducerProperty): KafkaProducer<String, String>

    fun createConsumer(
        consumerProperty: KafkaConsumerProperty,
        metadata: MessageConsumer,
        listener: MessageListener<*>,
        loggers: List<KafkaLogger>
    ): KafkaConsumer<String, String>
}
