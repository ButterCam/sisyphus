package com.bybutter.sisyphus.middleware.rocketmq

import org.apache.rocketmq.common.message.MessageExt

interface MessageListener<T> {
    suspend fun consumeMessage(messages: List<T>)
}

interface MessageConverter<T> {
    fun convert(message: MessageExt): T

    companion object : MessageConverter<MessageExt> {
        override fun convert(message: MessageExt): MessageExt {
            return message
        }
    }
}
