package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord

interface MessageListener<T> {
    suspend fun consumeMessage(messages: List<T>)

    fun shutdown() {}
}

interface MessageConverter<T> {
    fun convert(message: ConsumerRecord<String, String>): T

    companion object : MessageConverter<ConsumerRecord<String, String>> {
        override fun convert(message: ConsumerRecord<String, String>): ConsumerRecord<String, String> {
            return message
        }
    }
}
