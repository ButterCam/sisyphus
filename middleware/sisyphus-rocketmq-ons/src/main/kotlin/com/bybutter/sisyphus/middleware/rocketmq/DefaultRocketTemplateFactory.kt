package com.bybutter.sisyphus.middleware.rocketmq

import com.aliyun.openservices.ons.api.Consumer
import com.aliyun.openservices.ons.api.ONSFactory
import com.aliyun.openservices.ons.api.Producer
import com.aliyun.openservices.ons.api.PropertyKeyConst
import com.aliyun.openservices.ons.api.PullConsumer
import com.aliyun.openservices.ons.api.batch.BatchConsumer
import com.aliyun.openservices.ons.api.order.OrderConsumer
import com.aliyun.openservices.ons.api.order.OrderProducer
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker
import com.aliyun.openservices.ons.api.transaction.TransactionProducer
import java.util.Properties

class DefaultRocketTemplateFactory : RocketTemplateFactory {
    private var properties: Properties? = null

    override fun getMQProperties(property: RocketMQProperty): Properties {
        return properties?:Properties().apply {
            this.setProperty(PropertyKeyConst.AccessKey, property.accessKey)
            this.setProperty(PropertyKeyConst.SecretKey, property.secretKey)
            this.setProperty(PropertyKeyConst.NAMESRV_ADDR, property.nameServerAddr)
            this.setProperty(PropertyKeyConst.GROUP_ID, property.groupId)
            this.setProperty(PropertyKeyConst.InstanceName, property.instanceName)
            this.setProperty(PropertyKeyConst.INSTANCE_ID, property.instanceId)
            properties = this
        }
    }

    override fun createProducer(property: RocketMQProperty): Producer {
        return ONSFactory.createProducer(getMQProperties(property))
    }

    override fun createConsumer(property: RocketMQProperty): Consumer {
        return ONSFactory.createConsumer(getMQProperties(property))
    }

    override fun createBatchConsumer(property: RocketMQProperty): BatchConsumer {
        return ONSFactory.createBatchConsumer(getMQProperties(property))
    }

    override fun createOrderedConsumer(property: RocketMQProperty): OrderConsumer {
        return ONSFactory.createOrderedConsumer(getMQProperties(property))
    }

    override fun createPullConsumer(property: RocketMQProperty): PullConsumer {
        return ONSFactory.createPullConsumer(getMQProperties(property))
    }

    override fun createTransactionProducer(property: RocketMQProperty, checker: LocalTransactionChecker): TransactionProducer {
        return ONSFactory.createTransactionProducer(getMQProperties(property), checker)
    }

    override fun createOrderProducer(property: RocketMQProperty): OrderProducer {
        return ONSFactory.createOrderProducer(getMQProperties(property))
    }
}
