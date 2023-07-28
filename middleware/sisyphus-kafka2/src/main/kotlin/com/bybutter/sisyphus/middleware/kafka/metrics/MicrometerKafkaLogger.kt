package com.bybutter.sisyphus.middleware.kafka.metrics

import com.bybutter.sisyphus.middleware.kafka.KafkaListener
import com.bybutter.sisyphus.middleware.kafka.KafkaLogger
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class MicrometerKafkaLogger(private val registry: MeterRegistry) : KafkaLogger {
    override val id: String = MicrometerKafkaLogger::class.java.canonicalName

    override fun log(
        listener: KafkaListener<*, *>,
        consumer: KafkaConsumer<*, *>,
        message: ConsumerRecord<*, *>,
        costNanoTime: Long,
        exception: Exception?
    ) {
        registry.timer(
            "sisyphus_kafka_consumer",
            "topic", message.topic(),
            "partition", message.partition().toString(),
            "groupId", consumer.groupMetadata().groupId(),
            "memberId", consumer.groupMetadata().memberId(),
            "listener", listener.javaClass.canonicalName,
            "exception", exception?.javaClass?.canonicalName ?: "None"
        ).record(Duration.ofNanos(costNanoTime))
    }
}
