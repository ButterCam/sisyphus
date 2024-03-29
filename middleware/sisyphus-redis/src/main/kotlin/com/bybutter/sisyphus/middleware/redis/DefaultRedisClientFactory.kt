package com.bybutter.sisyphus.middleware.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI

open class DefaultRedisClientFactory : RedisClientFactory {
    private val clients: MutableMap<String, RedisClient> = hashMapOf()

    override fun createClient(property: RedisProperty): RedisClient {
        return createRedisClient(property.host, property.port, property)
    }

    protected open fun createRedisClient(
        host: String,
        port: Int,
        property: RedisProperty,
    ): RedisClient {
        return clients.getOrPut("$host:$port:${property.database}") {
            RedisClient.create(
                RedisURI.Builder.redis(host, port).withPassword(property.password.toCharArray())
                    .withDatabase(property.database).build(),
            )
        }
    }
}
