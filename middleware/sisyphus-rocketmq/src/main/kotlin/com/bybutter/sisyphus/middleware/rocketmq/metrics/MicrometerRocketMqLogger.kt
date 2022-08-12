package com.bybutter.sisyphus.middleware.rocketmq.metrics

import com.bybutter.sisyphus.middleware.rocketmq.MessageListener
import com.bybutter.sisyphus.middleware.rocketmq.RocketMqLogger
import io.micrometer.core.instrument.MeterRegistry
import org.apache.rocketmq.common.message.MessageExt
import java.time.Duration

class MicrometerRocketMqLogger(private val registry: MeterRegistry) : RocketMqLogger {
    override val id: String = MicrometerRocketMqLogger::class.java.canonicalName

    override fun log(
        topic: String,
        groupId: String,
        consumer: MessageListener<*>,
        messages: List<MessageExt>,
        costNanoTime: Long,
        exception: Exception?
    ) {
        registry.timer(
            "sisyphus_rocketmq_consumer",
            "topic", topic,
            "groupId", groupId,
            "consumer", consumer.javaClass.canonicalName,
            "tags", messages.firstOrNull()?.tags,
            "exception", exception?.javaClass?.canonicalName ?: "None"
        ).record(Duration.ofNanos(costNanoTime))
    }
}
