package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer

interface KafkaLogger {
    val id: String

    fun log(
        listener: KafkaListener<*, *>,
        consumer: KafkaConsumer<*, *>,
        message: ConsumerRecord<*, *>,
        costNanoTime: Long,
        exception: Exception?
    )
}
