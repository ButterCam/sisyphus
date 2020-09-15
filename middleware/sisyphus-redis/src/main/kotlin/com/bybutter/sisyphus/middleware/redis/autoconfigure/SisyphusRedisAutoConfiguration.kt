package com.bybutter.sisyphus.middleware.redis.autoconfigure

import com.bybutter.sisyphus.middleware.redis.DefaultRedisClientFactory
import com.bybutter.sisyphus.middleware.redis.RedisClientFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusRedisAutoConfiguration::class])
class SisyphusRedisAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [RedisClientFactory::class])
    fun defaultRedisClientFactory(): RedisClientFactory {
        return DefaultRedisClientFactory()
    }
}
