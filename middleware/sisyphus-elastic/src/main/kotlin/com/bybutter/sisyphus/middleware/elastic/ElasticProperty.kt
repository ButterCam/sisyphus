package com.bybutter.sisyphus.middleware.elastic

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class ElasticProperty(
    val name: String?,
    val protocol: String,
    val host: String,
    val port: Int,
    val userName: String? = null,
    val password: String? = null
)

data class ElasticProperties(
    @NestedConfigurationProperty
    val elastic: Map<String, ElasticProperty>
)
