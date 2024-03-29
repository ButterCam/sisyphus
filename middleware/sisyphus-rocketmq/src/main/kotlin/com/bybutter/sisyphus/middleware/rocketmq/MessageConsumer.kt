package com.bybutter.sisyphus.middleware.rocketmq

import org.apache.rocketmq.common.filter.ExpressionType
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
@Target(AnnotationTarget.CLASS)
annotation class MessageConsumer(
    val type: ConsumerType,
    val topic: String,
    val groupId: String = "",
    val filter: String = "*",
    val filterType: String = ExpressionType.TAG,
    val converter: KClass<out MessageConverter<*>> = MessageConverter.Companion::class,
)

enum class ConsumerType {
    ORDERLY,
    CONCURRENTLY,
}
