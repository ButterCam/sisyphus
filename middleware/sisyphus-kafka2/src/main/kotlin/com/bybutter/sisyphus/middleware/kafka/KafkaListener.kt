package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord

interface KafkaListener<K, V> {
    suspend fun consumeMessage(message: ConsumerRecord<K, V>)

    fun shutdown() {}
}
