package com.bybutter.sisyphus.middleware.jdbc

import javax.sql.DataSource
import org.jooq.Configuration
import org.jooq.SQLDialect

/**
 * Interceptor for specified Jooq configuration.
 */
interface JooqConfigInterceptor {
    /**
     * The Jooq configuration name, let it be null for global Jooq configuration.
     */
    val name: String?

    val qualifier: Class<*>?

    fun intercept(datasource: DataSource, dialect: SQLDialect, configuration: Configuration): Configuration
}
