package com.bybutter.sisyphus.middleware.jdbc

import com.bybutter.sisyphus.middleware.jdbc.transaction.SisyphusTransactionProvider
import io.seata.spring.annotation.GlobalTransactionScanner
import org.jooq.TransactionProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusJdbcAutoConfiguration::class])
class SisyphusJdbcAutoConfiguration {

    @Value("\${sisyphus.seata.applicationId}")
    private lateinit var applicationId: String

    @Value("\${sisyphus.seata.txServiceGroup}")
    private lateinit var txServiceGroup: String

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

    @Bean
    fun globalTransactionScanner(): GlobalTransactionScanner {
        return GlobalTransactionScanner(applicationId, txServiceGroup)
    }
}
