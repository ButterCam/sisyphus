package com.bybutter.sisyphus.middleware.rocketmq

import org.apache.rocketmq.common.message.MessageExt

interface RocketMqLogger {
    val id: String

    fun log(
        topic: String,
        groupId: String,
        consumer: MessageListener<*>,
        messages: List<MessageExt>,
        costNanoTime: Long,
        exception: Exception?,
    )
}
