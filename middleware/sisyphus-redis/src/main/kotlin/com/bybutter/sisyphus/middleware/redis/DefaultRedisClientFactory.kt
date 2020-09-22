package com.bybutter.sisyphus.middleware.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

open class DefaultRedisClientFactory : RedisClientFactory {
    private val clients: MutableMap<String, RedisClient> = hashMapOf()
    private val templates: MutableMap<String, StringRedisTemplate> = hashMapOf()

    override fun createClient(property: RedisProperty): RedisClient {
        return createRedisClient(property.host, property.port, property)
    }

    override fun createStringRedisTemplate(property: RedisProperty): StringRedisTemplate {
        return createTemplate(property.host, property.port, property)
    }

    protected open fun createRedisClient(host: String, port: Int, property: RedisProperty): RedisClient {
        return clients.getOrPut("$host:$port") {
            RedisClient.create(RedisURI.Builder.redis(host, port).withPassword(property.password).withDatabase(property.database ?: 0).build())
        }
    }

    protected fun createTemplate(host: String, port: Int, property: RedisProperty): StringRedisTemplate {
        return templates.getOrPut("$host:$port") {
            val redisStandaloneConfiguration = RedisStandaloneConfiguration(host, port)
            redisStandaloneConfiguration.password = RedisPassword.of(property.password)
            val lettuceConnectionFactory = LettuceConnectionFactory(
                    redisStandaloneConfiguration, LettuceClientConfiguration.builder().clientResources(createClient(property).resources).build()
            )
            lettuceConnectionFactory.afterPropertiesSet()
            return StringRedisTemplate(lettuceConnectionFactory)
        }
    }
}
