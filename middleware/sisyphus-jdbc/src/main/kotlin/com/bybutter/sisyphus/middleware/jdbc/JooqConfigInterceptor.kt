package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.Configuration
import org.jooq.SQLDialect
import javax.sql.DataSource

/**
 * Interceptor for specified Jooq configuration.
 */
interface JooqConfigInterceptor {
    /**
     * The Jooq configuration name, let it be null for global Jooq configuration.
     */
    val name: String?

    val qualifier: Class<*>?

    fun intercept(
        datasource: DataSource,
        dialect: SQLDialect,
        configuration: Configuration,
    ): Configuration
}
