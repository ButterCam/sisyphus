package com.bybutter.sisyphus.middleware.hbase.autoconfigure

import com.bybutter.sisyphus.middleware.hbase.DefaultHBaseTemplateFactory
import com.bybutter.sisyphus.middleware.hbase.HBaseTemplateFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusHBaseAutoConfiguration::class])
class SisyphusHBaseAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [HBaseTemplateFactory::class])
    fun defaultHBaseTemplateFactory(): HBaseTemplateFactory {
        return DefaultHBaseTemplateFactory()
    }
}
