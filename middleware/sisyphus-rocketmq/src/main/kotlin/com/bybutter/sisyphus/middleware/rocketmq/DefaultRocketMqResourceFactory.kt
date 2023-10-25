package com.bybutter.sisyphus.middleware.rocketmq

import com.bybutter.sisyphus.reflect.instance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.rocketmq.acl.common.AclClientRPCHook
import org.apache.rocketmq.acl.common.SessionCredentials
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer
import org.apache.rocketmq.client.consumer.MQConsumer
import org.apache.rocketmq.client.consumer.MessageSelector
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely
import org.apache.rocketmq.client.producer.DefaultMQProducer
import org.apache.rocketmq.client.producer.MQProducer
import org.apache.rocketmq.common.MixAll
import org.apache.rocketmq.common.filter.ExpressionType
import org.apache.rocketmq.common.message.MessageExt
import org.slf4j.LoggerFactory

open class DefaultRocketMqResourceFactory : RocketMqResourceFactory {
    override fun createProducer(producerProperty: RocketMqProducerProperty): MQProducer {
        val hook =
            if (producerProperty.aclAccessKey != null && producerProperty.aclSecretKey != null) {
                AclClientRPCHook(SessionCredentials(producerProperty.aclAccessKey, producerProperty.aclSecretKey))
            } else {
                null
            }
        return if (producerProperty.enableTrace) {
            DefaultMQProducer(MixAll.DEFAULT_PRODUCER_GROUP, hook, true, producerProperty.traceTopic)
        } else {
            DefaultMQProducer(MixAll.DEFAULT_PRODUCER_GROUP, hook)
        }.apply {
            this.namesrvAddr = chooseNameServerAddr(producerProperty)
            if (producerProperty.groupId != null) {
                this.producerGroup = producerProperty.groupId
            }
            if (producerProperty.accessChannel != null) {
                this.accessChannel = producerProperty.accessChannel
            }
        }
    }

    override fun createConsumer(
        consumerProperty: RocketMqConsumerProperty,
        metadata: MessageConsumer,
        listener: MessageListener<*>,
        loggers: List<RocketMqLogger>,
    ): MQConsumer {
        val listener = listener as MessageListener<Any?>
        val hook =
            if (consumerProperty.aclAccessKey != null && consumerProperty.aclSecretKey != null) {
                AclClientRPCHook(SessionCredentials(consumerProperty.aclAccessKey, consumerProperty.aclSecretKey))
            } else {
                null
            }

        return if (consumerProperty.enableTrace) {
            DefaultMQPushConsumer(
                metadata.groupId.takeIf { metadata.groupId.isNotEmpty() }
                    ?: MixAll.DEFAULT_CONSUMER_GROUP,
                hook,
                AllocateMessageQueueAveragely(),
                true,
                consumerProperty.traceTopic,
            )
        } else {
            DefaultMQPushConsumer(
                metadata.groupId.takeIf { metadata.groupId.isNotEmpty() }
                    ?: MixAll.DEFAULT_CONSUMER_GROUP,
                hook,
                AllocateMessageQueueAveragely(),
            )
        }.apply {
            this.namesrvAddr = chooseNameServerAddr(consumerProperty)
            if (consumerProperty.accessChannel != null) {
                this.accessChannel = consumerProperty.accessChannel
            }
            this.subscribe(
                metadata.topic,
                if (metadata.filterType == ExpressionType.TAG) {
                    MessageSelector.byTag(metadata.filter)
                } else {
                    MessageSelector.bySql(
                        metadata.filter,
                    )
                },
            )
            val converter = metadata.converter.instance()
            when (metadata.type) {
                ConsumerType.ORDERLY -> {
                    this.registerMessageListener { msgs: MutableList<MessageExt>, context: ConsumeOrderlyContext ->
                        val start = System.nanoTime()
                        try {
                            runBlocking(Dispatchers.IO) {
                                listener.consumeMessage(msgs.map { converter.convert(it) })
                            }
                            loggers.forEach {
                                it.log(metadata.topic, metadata.groupId, listener, msgs, System.nanoTime() - start, null)
                            }
                            ConsumeOrderlyStatus.SUCCESS
                        } catch (e: Exception) {
                            loggers.forEach {
                                it.log(metadata.topic, metadata.groupId, listener, msgs, System.nanoTime() - start, e)
                            }
                            ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT
                        }
                    }
                }

                ConsumerType.CONCURRENTLY -> {
                    this.registerMessageListener { msgs: MutableList<MessageExt>, context: ConsumeConcurrentlyContext ->
                        val start = System.nanoTime()
                        try {
                            runBlocking(Dispatchers.IO) {
                                listener.consumeMessage(msgs.map { converter.convert(it) })
                            }
                            loggers.forEach {
                                it.log(metadata.topic, metadata.groupId, listener, msgs, System.nanoTime() - start, null)
                            }
                            ConsumeConcurrentlyStatus.CONSUME_SUCCESS
                        } catch (e: Exception) {
                            loggers.forEach {
                                it.log(metadata.topic, metadata.groupId, listener, msgs, System.nanoTime() - start, e)
                            }
                            ConsumeConcurrentlyStatus.RECONSUME_LATER
                        }
                    }
                }
            }
        }
    }

    protected open fun chooseNameServerAddr(producerProperty: RocketMqProducerProperty): String {
        return producerProperty.nameServerAddr
    }

    protected open fun chooseNameServerAddr(consumerProperty: RocketMqConsumerProperty): String {
        return consumerProperty.nameServerAddr
    }

    companion object {
        private val listenerLogger = LoggerFactory.getLogger(MessageListener::class.java)
    }
}
