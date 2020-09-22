package com.bybutter.sisyphus.middleware.jdbc

import com.bybutter.sisyphus.middleware.jdbc.transaction.TransactionDelegatingDataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import org.jooq.tools.jdbc.JDBCUtils

abstract class AbstractDslContextFactory(private val configInterceptors: List<JooqConfigInterceptor>) : DslContextFactory {
    final override fun createContext(qualifier: Class<*>, property: JdbcDatabaseProperty): DSLContext {
        val url = buildJdbcUrl(property)
        val datasource = createDatasource(url, property)
        return DSL.using(createConfiguration(qualifier, datasource, JDBCUtils.dialect(url), configInterceptors))
    }

    protected open fun buildJdbcUrl(property: JdbcDatabaseProperty): String {
        return property.url
    }

    protected open fun createDatasource(url: String, property: JdbcDatabaseProperty): DataSource {
        return HikariDataSource(HikariConfig().apply {
            this.jdbcUrl = url
            this.username = property.userName
            this.password = property.password

            if (property.poolConfig?.poolName != null) {
                this.poolName = property.poolConfig.poolName
            }
            if (property.poolConfig?.minIdle != null) {
                this.minimumIdle = property.poolConfig.minIdle
            }
            if (property.poolConfig?.maxPoolSize != null) {
                this.maximumPoolSize = property.poolConfig.maxPoolSize
            }
            if (property.poolConfig?.maxLifetime != null) {
                this.maxLifetime = property.poolConfig.maxLifetime
            }
            if (property.poolConfig?.connectionTimeout != null) {
                this.connectionTimeout = property.poolConfig.connectionTimeout
            }
            if (property.poolConfig?.idleTimeout != null) {
                this.idleTimeout = property.poolConfig.idleTimeout
            }
            if (property.poolConfig?.validationTimeout != null) {
                this.validationTimeout = property.poolConfig.validationTimeout
            }
            if (property.poolConfig?.connectionInitSql != null) {
                this.connectionInitSql = property.poolConfig.connectionInitSql
            }
            if (property.poolConfig?.connectionTestQuery != null) {
                this.connectionTestQuery = property.poolConfig.connectionTestQuery
            }
        })
    }

    protected open fun createConfiguration(qualifier: Class<*>, datasource: DataSource, dialect: SQLDialect, interceptors: List<JooqConfigInterceptor>): Configuration {
        val config: Configuration = DefaultConfiguration().apply {
            set(dialect)
            set(TransactionDelegatingDataSource(datasource))
        }
        return interceptors.fold(config) { c, h ->
            if (h.qualifier == null || h.qualifier == qualifier){
                h.intercept(c)
            }else {
                c
            }
        }
    }
}
