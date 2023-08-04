package com.bybutter.sisyphus.middleware.elastic

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackageClasses = [SisyphusElasticAutoConfiguration::class])
class SisyphusElasticAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [ElasticClientFactory::class])
    fun defaultElasticClientFactory(): ElasticClientFactory {
        return DefaultElasticClientFactory()
    }
}
