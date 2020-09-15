package com.bybutter.sisyphus.middleware.elastic.autoconfigure

import com.bybutter.sisyphus.middleware.elastic.DefaultElasticClientFactory
import com.bybutter.sisyphus.middleware.elastic.ElasticClientFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusElasticAutoConfiguration::class])
class SisyphusElasticAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [ElasticClientFactory::class])
    fun defaultElasticClientFactory(): ElasticClientFactory {
        return DefaultElasticClientFactory()
    }
}
