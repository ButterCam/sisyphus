package com.bybutter.sisyphus.middleware.jdbc.test

import com.bybutter.sisyphus.middleware.jdbc.JdbcDatabaseProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddedDatasourceConfig {
    @Bean
    fun embeddedDatasource(): JdbcDatabaseProperty {
        return JdbcDatabaseProperty(url = "jdbc:h2:mem:test")
    }
}
