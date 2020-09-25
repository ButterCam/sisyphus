package com.bybutter.middleware.distributed.lock.autoconfigure

import io.lettuce.core.api.StatefulRedisConnection
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan(basePackageClasses = [RedisLockAutoConfiguration::class])
@Configuration
@ConditionalOnMissingBean(value = [StatefulRedisConnection::class])
@AutoConfigureAfter(name = ["com.bybutter.sisyphus.middleware.redis.autoconfigure.SisyphusRedisAutoConfiguration"])
class RedisLockAutoConfiguration
