package com.bybutter.sisyphus.middleware.sentinel.autoconfigure

import com.bybutter.sisyphus.middleware.sentinel.DefaultSentinelTemplateFactory
import com.bybutter.sisyphus.middleware.sentinel.SentinelTemplateFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusSentinelAutoConfiguration::class])
@AutoConfigureAfter(name = ["com.bybutter.sisyphus.middleware.redis.SisyphusRedisAutoConfiguration"])
class SisyphusSentinelAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [SentinelTemplateFactory::class])
    fun defaultSentinelTemplateFactory(): SentinelTemplateFactory {
        return DefaultSentinelTemplateFactory()
    }
}
