package com.bybutter.sisyphus.middleware.jdbc

import com.bybutter.sisyphus.middleware.jdbc.transaction.SisyphusTransactionProvider
import org.jooq.TransactionProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusJdbcAutoConfiguration::class])
class SisyphusJdbcAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [TransactionProvider::class])
    fun sisyphusTransactionProvider(): TransactionProvider {
        return SisyphusTransactionProvider()
    }
}
