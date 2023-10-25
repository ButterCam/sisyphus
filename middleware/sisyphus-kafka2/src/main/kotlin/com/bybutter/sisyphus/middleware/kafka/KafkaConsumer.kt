package com.bybutter.sisyphus.middleware.kafka

import com.bybutter.sisyphus.middleware.kafka.serialization.JsonDeserializer
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
@Target(AnnotationTarget.CLASS)
annotation class KafkaConsumer(
    val topics: Array<String> = [],
    val topicPattern: String = "",
    val groupId: String = "",
    val keyDeserializer: KClass<out Deserializer<*>> = StringDeserializer::class,
    val valueDeserializer: KClass<out Deserializer<*>> = JsonDeserializer::class,
    val errorHandler: KClass<out KafkaExceptionHandler> = DefaultExceptionHandler::class,
)
