package com.bybutter.middleware.distributed.lock.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate

@ComponentScan(basePackageClasses = [RedisLockAutoConfiguration::class])
@Configuration
@ConditionalOnMissingBean(value = [StringRedisTemplate::class])
@AutoConfigureAfter(name = ["com.bybutter.sisyphus.middleware.redis.autoconfigure.SisyphusRedisAutoConfiguration"])
class RedisLockAutoConfiguration
