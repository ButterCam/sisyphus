package com.bybutter.sisyphus.middleware.mongo

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class MongoDatabaseProperty(
    val url: String,
    val publicUrl: String,
    val qualifier: Class<*>
)

data class MongoDatabaseProperties(
    @NestedConfigurationProperty
    val mongo: Map<String, MongoDatabaseProperty>
)
