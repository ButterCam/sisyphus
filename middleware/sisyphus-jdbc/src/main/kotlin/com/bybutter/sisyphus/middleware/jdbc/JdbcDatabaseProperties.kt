package com.bybutter.sisyphus.middleware.jdbc

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class JdbcDatabaseProperties(
    @NestedConfigurationProperty
    val jdbc: Map<String, JdbcDatabaseProperty>
)
