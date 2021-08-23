package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.SQLDialect
import org.springframework.boot.context.properties.NestedConfigurationProperty

data class JdbcDatabaseProperty(
    val url: String,
    val userName: String? = null,
    val password: String? = null,
    val parameters: Map<String, String> = mapOf(),
    @NestedConfigurationProperty
    val poolConfig: ConnectionPoolProperty? = null,
    val qualifier: Class<*>,
    val dialect: SQLDialect? = null,
)

data class ConnectionPoolProperty(
    val poolName: String?,

    val minIdle: Int?,
    val maxPoolSize: Int?,

    val maxLifetime: Long?,
    val connectionTimeout: Long?,
    val idleTimeout: Long?,
    val validationTimeout: Long?,

    val connectionInitSql: String?,
    val connectionTestQuery: String?
)
