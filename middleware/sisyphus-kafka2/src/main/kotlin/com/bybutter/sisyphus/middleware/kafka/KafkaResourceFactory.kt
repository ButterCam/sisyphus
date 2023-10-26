package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

interface KafkaResourceFactory {
    fun createProducer(producerProperty: KafkaProducerProperty): KafkaProducer<*, *>

    fun createConsumer(
        consumerProperty: KafkaConsumerProperty,
        metadata: com.bybutter.sisyphus.middleware.kafka.KafkaConsumer,
        listener: KafkaListener<*, *>,
        loggers: List<KafkaLogger>,
    ): KafkaConsumer<*, *>
}
