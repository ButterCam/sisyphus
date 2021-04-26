package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.DSLContext
import javax.sql.DataSource

interface DslContextFactory {
    fun createContext(qualifier: Class<*>, property: JdbcDatabaseProperty): DSLContext
    fun createDatasource(qualifier: Class<*>, property: JdbcDatabaseProperty): DataSource
}
