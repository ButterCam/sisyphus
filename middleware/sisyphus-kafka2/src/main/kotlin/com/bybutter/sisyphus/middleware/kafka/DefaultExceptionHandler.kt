package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class DefaultExceptionHandler(private val retryTimes: Int) : KafkaExceptionHandler {
    private val retryCount = ConcurrentHashMap<String, AtomicInteger>()

    constructor() : this(20)

    override fun onException(record: ConsumerRecord<*, *>, exception: Exception): KafkaExceptionPolicy {
        val key = "${record.topic()}/${record.partition()}/${record.offset()}"

        val errorCount = retryCount.getOrPut(key) { AtomicInteger(0) }.incrementAndGet()

        return if (errorCount <= retryTimes) {
            KafkaExceptionPolicy.RETRY
        } else {
            retryCount.remove(key)
            KafkaExceptionPolicy.SKIP
        }
    }
}
