package com.bybutter.sisyphus.middleware.elastic

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class ElasticProperty(
    val qualifier: Class<*>,
    val protocol: String,
    val host: String,
    val port: Int,
    val userName: String? = null,
    val password: String? = null,
    val extensions: Map<String, Any> = mapOf(),
)

data class ElasticProperties(
    @NestedConfigurationProperty
    val elastic: Map<String, ElasticProperty>,
)
