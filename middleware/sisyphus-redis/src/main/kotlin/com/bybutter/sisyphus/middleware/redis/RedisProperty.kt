package com.bybutter.sisyphus.middleware.redis

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class RedisProperty(
    val qualifier: Class<*>,
    val host: String,
    val port: Int,
    val password: String,
    val database: Int = 0,
    val extensions: Map<String, Any> = mapOf()
)

data class RedisProperties(
    @NestedConfigurationProperty
    val redis: Map<String, RedisProperty>
)
