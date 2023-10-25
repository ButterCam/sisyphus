package com.bybutter.sisyphus.middleware.mongo

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class MongoDatabaseProperty(
    val url: String,
    val qualifier: Class<*>,
    val extensions: Map<String, Any> = mapOf(),
)

data class MongoDatabaseProperties(
    @NestedConfigurationProperty
    val mongo: Map<String, MongoDatabaseProperty>,
)
