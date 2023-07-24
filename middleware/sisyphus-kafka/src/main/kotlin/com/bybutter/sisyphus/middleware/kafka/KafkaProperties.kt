package com.bybutter.sisyphus.middleware.kafka

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class KafkaProducerProperty(
    val qualifier: Class<*>,
    val nameServerAddr: String,
    val traceTopic: String? = null,
    val protocol: String? = null,
    val mechanism: String? = null,
    val userName: String? = null,
    val password: String? = null,
    val maxBlockTime: String? = null,
    val retries: String? = null,
    val retryInterval: String? = null,
    val extensions: Map<String, Any> = mapOf()
)

data class KafkaConsumerProperty(
    val qualifier: Class<*>,
    val nameServerAddr: String,
    val traceTopic: String? = null,
    val protocol: String? = null,
    val mechanism: String? = null,
    val groupId: String? = null,
    val userName: String? = null,
    val password: String? = null,
    val autoCommitIntervalMs: String?= null,
    val pollMaxRecords: String? = null,
    val pollInterval: String? = null,
    val extensions: Map<String, Any> = mapOf()
)

data class KafkaProperties(
    @NestedConfigurationProperty
    val consumers: Map<String, KafkaConsumerProperty>,
    @NestedConfigurationProperty
    val producers: Map<String, KafkaProducerProperty>
)
