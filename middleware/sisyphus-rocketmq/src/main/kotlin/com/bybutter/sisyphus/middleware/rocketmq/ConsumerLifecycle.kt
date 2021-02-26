package com.bybutter.sisyphus.middleware.rocketmq

import org.apache.rocketmq.client.consumer.MQConsumer
import org.apache.rocketmq.client.consumer.MQPullConsumer
import org.apache.rocketmq.client.consumer.MQPushConsumer
import org.springframework.context.SmartLifecycle

class ConsumerLifecycle(private val consumer: MQConsumer) : SmartLifecycle {
    private var running = false

    override fun isRunning(): Boolean {
        return running
    }

    override fun start() {
        when (consumer) {
            is MQPushConsumer -> consumer.start()
            is MQPullConsumer -> consumer.start()
        }
        running = true
    }

    override fun stop() {
        when (consumer) {
            is MQPushConsumer -> consumer.shutdown()
            is MQPullConsumer -> consumer.shutdown()
        }
        running = false
    }
}
