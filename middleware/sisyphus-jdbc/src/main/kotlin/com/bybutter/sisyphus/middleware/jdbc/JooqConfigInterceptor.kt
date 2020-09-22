package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.Configuration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Interceptor for specified Jooq configuration.
 */
interface JooqConfigInterceptor {
    /**
     * The Jooq configuration name, let it be null for global Jooq configuration.
     */
    val name: String?

    val qualifier: Class<*>?

    fun intercept(configuration: Configuration): Configuration
}