package com.bybutter.sisyphus.middleware.cache.redis

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusRedisAutoConfiguration::class])
class SisyphusRedisAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [RedisFactory::class])
    fun defaultRedisClientFactory(): RedisFactory {
        return DefaultRedisFactory()
    }
}
