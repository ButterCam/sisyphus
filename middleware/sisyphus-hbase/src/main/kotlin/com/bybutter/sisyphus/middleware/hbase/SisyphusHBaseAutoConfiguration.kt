package com.bybutter.sisyphus.middleware.hbase

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusHBaseAutoConfiguration::class])
class SisyphusHBaseAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [HBaseTemplateFactory::class])
    fun defaultHBaseTemplateFactory(): HBaseTemplateFactory {
        return DefaultHBaseTemplateFactory()
    }
}
