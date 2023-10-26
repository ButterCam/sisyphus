package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.DSLContext

interface DslContextFactory {
    fun createContext(
        qualifier: Class<*>,
        property: JdbcDatabaseProperty,
    ): DSLContext
}
