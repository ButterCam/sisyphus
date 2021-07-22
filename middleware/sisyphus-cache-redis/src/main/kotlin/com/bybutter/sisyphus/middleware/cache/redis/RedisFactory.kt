package com.bybutter.sisyphus.middleware.cache.redis

import io.lettuce.core.RedisClient

interface RedisFactory {
    fun createClient(property: RedisProperty): RedisClient
}
