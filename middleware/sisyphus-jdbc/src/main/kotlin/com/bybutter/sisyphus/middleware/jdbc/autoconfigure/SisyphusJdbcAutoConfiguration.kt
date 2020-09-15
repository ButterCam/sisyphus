package com.bybutter.sisyphus.middleware.jdbc.autoconfigure

import com.bybutter.sisyphus.middleware.jdbc.AbstractDslContextFactory
import com.bybutter.sisyphus.middleware.jdbc.DslContextFactory
import com.bybutter.sisyphus.middleware.jdbc.JooqConfigInterceptor
import com.bybutter.sisyphus.middleware.jdbc.transaction.SisyphusTransactionProvider
import org.jooq.TransactionProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusJdbcAutoConfiguration::class])
class SisyphusJdbcAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [TransactionProvider::class])
    fun sisyphusTransactionProvider(): TransactionProvider {
        return SisyphusTransactionProvider()
    }

    @Bean
    @ConditionalOnMissingBean(value = [DslContextFactory::class])
    fun defaultDslContextFactory(configInterceptors: List<JooqConfigInterceptor>): DslContextFactory {
        return object : AbstractDslContextFactory(configInterceptors) {}
    }
}
