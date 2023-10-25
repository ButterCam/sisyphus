package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord

interface KafkaExceptionHandler {
    fun onException(
        record: ConsumerRecord<*, *>,
        exception: Exception,
    ): KafkaExceptionPolicy
}
