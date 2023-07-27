package com.bybutter.sisyphus.middleware.kafka

import com.bybutter.sisyphus.middleware.kafka.serialization.LISTENER_CONFIG
import com.bybutter.sisyphus.middleware.kafka.serialization.LISTENER_KEY_TYPE
import com.bybutter.sisyphus.middleware.kafka.serialization.LISTENER_VALUE_TYPE
import com.bybutter.sisyphus.reflect.getTypeArgument
import com.bybutter.sisyphus.reflect.instance
import com.bybutter.sisyphus.security.base64Decode
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.Properties

open class DefaultKafkaResourceFactory : KafkaResourceFactory {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun createProducer(
        producerProperty: KafkaProducerProperty
    ): KafkaProducer<*, *> {
        val properties = Properties().configServerProperties(producerProperty)
        producerProperty.acks?.let {
            properties[ProducerConfig.ACKS_CONFIG] = it
        }

        producerProperty.properties.forEach { (k, v) ->
            if (k in ProducerConfig.configNames()) {
                properties[k] = v
            } else {
                logger.warn("Unknown producer config for ${producerProperty.qualifier}: $k")
            }
        }

        return KafkaProducer(
            properties,
            producerProperty.keySerializer.instance().apply { configure(producerProperty.keySerializerConfig, true) },
            producerProperty.valueSerializer.instance()
                .apply { configure(producerProperty.valueSerializerConfig, false) }
        )
    }

    override fun createConsumer(
        consumerProperty: KafkaConsumerProperty,
        metadata: com.bybutter.sisyphus.middleware.kafka.KafkaConsumer,
        listener: KafkaListener<*, *>,
        loggers: List<KafkaLogger>
    ): KafkaConsumer<*, *> {
        val properties = Properties().configServerProperties(consumerProperty)

        properties[ConsumerConfig.GROUP_ID_CONFIG] = consumerProperty.groupId ?: metadata.groupId

        consumerProperty.properties.forEach { (k, v) ->
            if (k in ConsumerConfig.configNames()) {
                properties[k] = v
            } else {
                logger.warn("Unknown consumer config for ${consumerProperty.qualifier}: $k")
            }
        }

        properties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false

        val config = buildMap<String, Any?> {
            put(LISTENER_CONFIG, listener)
            listener.javaClass.getTypeArgument(KafkaListener::class.java, 0).let {
                put(LISTENER_KEY_TYPE, it)
            }
            listener.javaClass.getTypeArgument(KafkaListener::class.java, 1).let {
                put(LISTENER_VALUE_TYPE, it)
            }
        }

        return KafkaConsumer(
            properties,
            metadata.keyDeserializer.instance().apply { configure(config, true) },
            metadata.valueDeserializer.instance().apply { configure(config, false) }
        ).also {
            if (metadata.topics.isNotEmpty()) {
                it.subscribe(metadata.topics.toList())
            } else if (metadata.topicPattern.isNotEmpty()) {
                it.subscribe(metadata.topicPattern.toPattern())
            } else {
                throw IllegalArgumentException("No topics or topic pattern specified for ${consumerProperty.qualifier}.")
            }
        }
    }

    private fun Properties.configServerProperties(
        property: KafkaServerProperty
    ): Properties {
        this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = property.bootstrapServers
        this[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = property.protocol
        if (property.protocol?.startsWith("SASL_") == true) {
            this[SaslConfigs.SASL_JAAS_CONFIG] = property.saslConfig
            this[SaslConfigs.SASL_MECHANISM] = property.saslMechanism
        }
        if (property.protocol?.contains("SSL") == true) {
            property.sslTruststore?.base64Decode()?.let {
                val file = Files.createTempFile("kafka-truststore", ".jks").toFile().apply {
                    writeBytes(it)
                    deleteOnExit()
                }
                this[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = file.absolutePath
            }
            property.sslTruststorePassword?.let {
                this[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = it
            }
            property.sslEndpointIdentificationAlgorithm?.let {
                this[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = it
            }
        }
        return this
    }

    companion object {
        private val listenerLogger = LoggerFactory.getLogger(KafkaListener::class.java)
    }
}
