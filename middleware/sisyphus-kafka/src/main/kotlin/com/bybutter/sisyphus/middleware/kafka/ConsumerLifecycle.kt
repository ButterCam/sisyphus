package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.springframework.context.SmartLifecycle

class ConsumerLifecycle(private val consumer: KafkaConsumer<String, String>, private val listener: MessageListener<*>) : SmartLifecycle {
    private var running = false

    override fun isRunning(): Boolean {
        return running
    }

    override fun start() {
        running = true
    }

    override fun stop() {
        listener.shutdown()
        running = false
    }
}
