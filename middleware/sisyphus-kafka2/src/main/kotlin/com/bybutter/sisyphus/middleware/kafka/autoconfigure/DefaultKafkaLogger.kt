package com.bybutter.sisyphus.middleware.kafka.autoconfigure

import com.bybutter.sisyphus.middleware.kafka.KafkaListener
import com.bybutter.sisyphus.middleware.kafka.KafkaLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class DefaultKafkaLogger : KafkaLogger {
    override val id: String = KafkaLogger::class.java.typeName

    override fun log(
        listener: KafkaListener<*, *>,
        consumer: KafkaConsumer<*, *>,
        message: ConsumerRecord<*, *>,
        costNanoTime: Long,
        exception: Exception?
    ) {
        if (exception == null) {
            logger.info(
                "[COMPLETED] ${consumer.groupMetadata().memberId()} <- ${message.topic()}/${message.partition()}/${message.offset()} +${getCostString(costNanoTime)}"
            )
        } else {
            logger.error(
                "[ERROR] ${consumer.groupMetadata().memberId()} <- ${message.topic()}/${message.partition()}/${message.offset()} +${getCostString(costNanoTime)}",
                exception
            )
        }
    }

    protected fun getCostString(cost: Long): String {
        return when {
            cost < TimeUnit.MICROSECONDS.toNanos(1) -> {
                "${cost}ns"
            }

            cost < TimeUnit.MILLISECONDS.toNanos(1) -> {
                "${cost / 1000.0}µs"
            }

            cost < TimeUnit.SECONDS.toNanos(1) -> {
                String.format("%.3fms", cost / 1000000.0)
            }

            else -> {
                String.format("%.3fs", cost / 1000000000.0)
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger("Kafka")
    }
}
