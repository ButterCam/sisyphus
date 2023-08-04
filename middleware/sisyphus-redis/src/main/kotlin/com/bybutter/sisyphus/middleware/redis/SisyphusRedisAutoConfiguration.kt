package com.bybutter.sisyphus.middleware.redis

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackageClasses = [SisyphusRedisAutoConfiguration::class])
class SisyphusRedisAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [RedisClientFactory::class])
    fun defaultRedisClientFactory(): RedisClientFactory {
        return DefaultRedisClientFactory()
    }
}
