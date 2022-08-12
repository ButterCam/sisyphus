package com.bybutter.sisyphus.middleware.rocketmq.autoconfigure

import com.bybutter.sisyphus.middleware.rocketmq.MessageListener
import com.bybutter.sisyphus.middleware.rocketmq.RocketMqLogger
import org.apache.rocketmq.common.message.MessageExt
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class DefaultRocketMqLogger : RocketMqLogger {
    override val id: String = RocketMqLogger::class.java.typeName

    override fun log(
        topic: String,
        groupId: String,
        consumer: MessageListener<*>,
        messages: List<MessageExt>,
        costNanoTime: Long,
        exception: Exception?
    ) {
        if (messages.isEmpty()) return
        val firstMessage = messages.first()

        if (exception == null) {
            logger.info("[COMPLETED] $groupId <- $topic/${messages.joinToString { it.msgId }}(${firstMessage.tags}) +${getCostString(costNanoTime)}")
        } else {
            logger.error("[ERROR] $groupId <- $topic/${messages.joinToString { it.msgId }}(${firstMessage.tags}) +${getCostString(costNanoTime)}", exception)
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
        val logger = LoggerFactory.getLogger("RocketMQ")
    }
}
