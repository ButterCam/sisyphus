package com.bybutter.sisyphus.middleware.jdbc

import javax.sql.DataSource
import org.jooq.DSLContext

interface DslContextFactory {
    fun createContext(qualifier: Class<*>, property: JdbcDatabaseProperty): DSLContext
    fun createDatasource(qualifier: Class<*>, property: JdbcDatabaseProperty): DataSource
}
