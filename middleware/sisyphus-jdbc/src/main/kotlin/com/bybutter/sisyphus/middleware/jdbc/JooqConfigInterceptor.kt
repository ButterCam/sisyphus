package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.Configuration

/**
 * Interceptor for specified Jooq configuration.
 */
interface JooqConfigInterceptor {
    /**
     * The Jooq configuration name, let it be null for global Jooq configuration.
     */
    val name: String?

    fun intercept(configuration: Configuration): Configuration
}
