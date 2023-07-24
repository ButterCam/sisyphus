package com.bybutter.sisyphus.middleware.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.slf4j.LoggerFactory
import java.util.Collections
import java.util.Properties


open class DefaultKafkaResourceFactory : KafkaResourceFactory {
    override fun createProducer(producerProperty: KafkaProducerProperty): KafkaProducer<String, String> {
        val properties = Properties()
        // Kafka消息的序列化方式。
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        properties.put("bootstrap.servers", producerProperty.nameServerAddr)
        properties.put("security.protocol", producerProperty.protocol)
        properties.put("sasl.mechanism", producerProperty.mechanism)
        // 请求的最长等待时间ms 30 * 1000。
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, producerProperty.maxBlockTime)
        // 设置客户端内部重试次数。
        properties.put(ProducerConfig.RETRIES_CONFIG, producerProperty.retries)
        // 设置客户端内部重试间隔。
        properties.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, producerProperty.retryInterval)

        properties.put("java.security.auth.login.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"${producerProperty.userName}\" password=\"${producerProperty.password}\"")

        return KafkaProducer<String, String>(properties)
    }

    override fun createConsumer(
        consumerProperty: KafkaConsumerProperty,
        metadata: MessageConsumer,
        listener: MessageListener<*>,
        loggers: List<KafkaLogger>
    ): KafkaConsumer<String, String> {
        val properties = Properties()
        // Kafka消息的序列化方式
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
        // 指定消费组id
        // 指定消费组id
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProperty.groupId)
        // enable.auto.commit如果为true，则消费者的偏移量将定期在后台提交。
        // enable.auto.commit如果为true，则消费者的偏移量将定期在后台提交。
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        // 重置消费位点策略:earliest、latest、none
        // 重置消费位点策略:earliest、latest、none
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        // 设置kafka自动提交offset的频率，默认5000ms，也就是5s
        // 设置kafka自动提交offset的频率，默认5000ms，也就是5s
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, consumerProperty.autoCommitIntervalMs)
        // 设置消费者在一次poll中返回的最大记录数
        // 设置消费者在一次poll中返回的最大记录数
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerProperty.pollMaxRecords)
        // 设置消费者两次poll的最大时间间隔
        // 设置消费者两次poll的最大时间间隔
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, consumerProperty.pollInterval)
        // 构建KafkaConsumer对象
        val kafkaConsumer = KafkaConsumer<String, String>(properties)
        kafkaConsumer.subscribe(Collections.singleton(consumerProperty.traceTopic))
        return kafkaConsumer
    }

    protected open fun chooseNameServerAddr(producerProperty: KafkaProducerProperty): String {
        return producerProperty.nameServerAddr
    }

    protected open fun chooseNameServerAddr(consumerProperty: KafkaConsumerProperty): String {
        return consumerProperty.nameServerAddr
    }

    companion object {
        private val listenerLogger = LoggerFactory.getLogger(MessageListener::class.java)
    }
}
