package com.bybutter.sisyphus.middleware.redis

import io.lettuce.core.RedisClient

interface RedisClientFactory {
    fun createClient(property: RedisProperty): RedisClient
}
