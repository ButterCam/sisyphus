package com.bybutter.sisyphus.middleware.rocketmq

import com.aliyun.openservices.ons.api.Consumer
import com.aliyun.openservices.ons.api.Producer
import com.aliyun.openservices.ons.api.PullConsumer
import com.aliyun.openservices.ons.api.batch.BatchConsumer
import com.aliyun.openservices.ons.api.order.OrderConsumer
import com.aliyun.openservices.ons.api.order.OrderProducer
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker
import com.aliyun.openservices.ons.api.transaction.TransactionProducer
import java.util.Properties

interface RocketTemplateFactory {
    fun getMQProperties(property: RocketMQProperty): Properties
    fun createProducer(property: RocketMQProperty): Producer
    fun createConsumer(property: RocketMQProperty): Consumer
    fun createBatchConsumer(property: RocketMQProperty): BatchConsumer
    fun createOrderedConsumer(property: RocketMQProperty): OrderConsumer
    fun createPullConsumer(property: RocketMQProperty): PullConsumer
    fun createTransactionProducer(property: RocketMQProperty, checker: LocalTransactionChecker): TransactionProducer
    fun createOrderProducer(property: RocketMQProperty): OrderProducer
}
