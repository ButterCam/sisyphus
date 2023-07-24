package com.bybutter.sisyphus.middleware.kafka.metrics

import com.bybutter.sisyphus.middleware.kafka.KafkaLogger
import com.bybutter.sisyphus.middleware.kafka.MessageListener
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration

class MicrometerKafkaLogger(private val registry: MeterRegistry) : KafkaLogger {
    override val id: String = MicrometerKafkaLogger::class.java.canonicalName

    override fun log(
        topic: String,
        groupId: String,
        consumer: MessageListener<*>,
        messages: List<ConsumerRecord<String, String>>,
        costNanoTime: Long,
        exception: Exception?
    ) {
        registry.timer(
            "sisyphus_kafka_consumer",
            "topic", topic,
            "groupId", groupId,
            "consumer", consumer.javaClass.canonicalName,
            "tags", messages.firstOrNull()?.key(),
            "exception", exception?.javaClass?.canonicalName ?: "None"
        ).record(Duration.ofNanos(costNanoTime))
    }
}
