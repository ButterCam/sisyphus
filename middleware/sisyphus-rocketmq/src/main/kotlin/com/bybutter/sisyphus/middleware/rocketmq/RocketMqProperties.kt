package com.bybutter.sisyphus.middleware.rocketmq

import org.apache.rocketmq.client.AccessChannel
import org.springframework.boot.context.properties.NestedConfigurationProperty

data class RocketMqConsumerProperty(
    val qualifier: Class<*>,
    val nameServerAddr: String,
    val publicNameServerAddr: String,
    val aclAccessKey: String? = null,
    val aclSecretKey: String? = null,
    val enableTrace: Boolean = false,
    val traceTopic: String? = null,
    val accessChannel: AccessChannel? = null
)

data class RocketMqProducerProperty(
    val qualifier: Class<*>,
    val nameServerAddr: String,
    val publicNameServerAddr: String,
    val aclAccessKey: String? = null,
    val aclSecretKey: String? = null,
    val groupId: String? = null,
    val enableTrace: Boolean = false,
    val traceTopic: String? = null,
    val accessChannel: AccessChannel? = null
)

data class RocketMqProperties(
    @NestedConfigurationProperty
    val consumers: Map<String, RocketMqConsumerProperty>,
    @NestedConfigurationProperty
    val producers: Map<String, RocketMqProducerProperty>
)
