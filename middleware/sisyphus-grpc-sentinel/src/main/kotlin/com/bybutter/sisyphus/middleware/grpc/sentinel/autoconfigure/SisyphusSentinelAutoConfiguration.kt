package com.bybutter.sisyphus.middleware.grpc.sentinel.autoconfigure

import com.bybutter.sisyphus.middleware.grpc.sentinel.DefaultSentinelTemplateFactory
import com.bybutter.sisyphus.middleware.grpc.sentinel.SentinelTemplateFactory
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
