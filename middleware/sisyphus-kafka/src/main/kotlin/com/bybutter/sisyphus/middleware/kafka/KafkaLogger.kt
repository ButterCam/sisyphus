package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord

interface KafkaLogger {
    val id: String

    fun log(
        topic: String,
        groupId: String,
        consumer: MessageListener<*>,
        messages: List<ConsumerRecord<String, String>>,
        costNanoTime: Long,
        exception: Exception?
    )
}
