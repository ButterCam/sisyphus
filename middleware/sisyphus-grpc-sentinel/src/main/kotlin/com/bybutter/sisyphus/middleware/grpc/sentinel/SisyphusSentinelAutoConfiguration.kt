package com.bybutter.sisyphus.middleware.grpc.sentinel

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusSentinelAutoConfiguration::class])
class SisyphusSentinelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = [SentinelTemplateFactory::class])
    fun defaultSentinelTemplateFactory(): SentinelTemplateFactory {
        return DefaultSentinelTemplateFactory()
    }
}
