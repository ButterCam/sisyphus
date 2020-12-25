package com.bybutter.sisyphus.middleware.seata

import com.bybutter.sisyphus.middleware.jdbc.DslContextFactory
import com.bybutter.sisyphus.middleware.jdbc.JooqConfigInterceptor
import io.seata.spring.annotation.GlobalTransactionScanner
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusSeataAutoConfiguration::class])
@AutoConfigureBefore(name = ["com.bybutter.sisyphus.middleware.jdbc.SisyphusJdbcAutoConfiguration"])
class SisyphusSeataAutoConfiguration {

    @Value("\${sisyphus.seata.applicationId:default}")
    private lateinit var applicationId: String

    @Value("\${sisyphus.seata.txServiceGroup:default}")
    private lateinit var txServiceGroup: String

    @Bean
    @ConditionalOnMissingBean(value = [DslContextFactory::class])
    fun defaultDslContextFactory(configInterceptors: List<JooqConfigInterceptor>): DslContextFactory {
        return object : SeataAbstractDslContextFactory(configInterceptors) {}
    }

    @Bean
    fun globalTransactionScanner(): GlobalTransactionScanner {
        return GlobalTransactionScanner(applicationId, txServiceGroup)
    }
}
