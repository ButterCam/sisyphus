package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.DSLContext

interface DslContextFactory {
    fun createContext(name: String, property: JdbcDatabaseProperty): DSLContext
}
