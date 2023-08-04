package com.bybutter.sisyphus.middleware.hbase

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackageClasses = [SisyphusHBaseAutoConfiguration::class])
class SisyphusHBaseAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [HBaseTemplateFactory::class])
    fun defaultHBaseTemplateFactory(): HBaseTemplateFactory {
        return DefaultHBaseTemplateFactory()
    }
}
