package com.bybutter.sisyphus.middleware.redis

import io.lettuce.core.RedisClient
import org.springframework.data.redis.core.StringRedisTemplate

interface RedisClientFactory {
    fun createClient(property: RedisProperty): RedisClient
    fun createStringRedisTemplate(property: RedisProperty): StringRedisTemplate
}
