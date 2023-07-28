package com.bybutter.sisyphus.middleware.kafka

import com.bybutter.sisyphus.middleware.kafka.serialization.JsonSerializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.context.properties.NestedConfigurationProperty

interface KafkaServerProperty {
    val bootstrapServers: String
    val protocol: String?
    val saslConfig: String?
    val saslMechanism: String?
    val sslTruststore: String?
    val sslTruststorePassword: String?
    val sslEndpointIdentificationAlgorithm: String?
}

data class KafkaProducerProperty(
    val qualifier: Class<*>,
    override val bootstrapServers: String,
    val keySerializer: Class<out Serializer<*>> = StringSerializer::class.java,
    val valueSerializer: Class<out Serializer<*>> = JsonSerializer::class.java,
    val acks: String? = null,
    // PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL
    override val protocol: String? = null,
    override val saslConfig: String? = null,
    override val saslMechanism: String? = null,
    override val sslTruststore: String? = null,
    override val sslTruststorePassword: String? = null,
    override val sslEndpointIdentificationAlgorithm: String? = null,
    val properties: Map<String, Any?> = mapOf(),
    val keySerializerConfig: Map<String, Any?> = mapOf(),
    val valueSerializerConfig: Map<String, Any?> = mapOf()
) : KafkaServerProperty

data class KafkaConsumerProperty(
    val qualifier: Class<*>,
    override val bootstrapServers: String,
    // PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL
    val groupId: String? = null,
    override val protocol: String? = null,
    override val saslConfig: String? = null,
    override val saslMechanism: String? = null,
    override val sslTruststore: String? = null,
    override val sslTruststorePassword: String? = null,
    override val sslEndpointIdentificationAlgorithm: String? = null,
    val properties: Map<String, Any?> = mapOf()
) : KafkaServerProperty

data class KafkaProperties(
    @NestedConfigurationProperty
    val producers: Map<String, KafkaProducerProperty>,

    @NestedConfigurationProperty
    val consumers: Map<String, KafkaConsumerProperty>
)
