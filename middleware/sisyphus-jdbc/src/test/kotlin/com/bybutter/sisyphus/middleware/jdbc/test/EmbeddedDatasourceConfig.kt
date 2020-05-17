package com.bybutter.sisyphus.middleware.jdbc.test

import com.bybutter.sisyphus.middleware.jdbc.DefaultDslContextFactory
import com.bybutter.sisyphus.middleware.jdbc.DslContextFactory
import com.bybutter.sisyphus.middleware.jdbc.JdbcDatabaseProperty
import com.bybutter.sisyphus.middleware.jdbc.JooqConfigInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddedDatasourceConfig {
    @Bean
    fun dslContextFactory(@Autowired configInterceptors: List<JooqConfigInterceptor>): DslContextFactory {
        return DefaultDslContextFactory(configInterceptors)
    }

    @Bean
    fun embeddedDatasource(): JdbcDatabaseProperty {
        return JdbcDatabaseProperty(url = "jdbc:h2:mem:test")
    }
}
