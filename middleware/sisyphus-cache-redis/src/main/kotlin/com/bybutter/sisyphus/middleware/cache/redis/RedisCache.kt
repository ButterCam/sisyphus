package com.bybutter.sisyphus.middleware.cache.redis

import com.bybutter.sisyphus.middleware.cache.CacheProvider
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.lettuce.core.RedisClient
import io.lettuce.core.SetArgs
import io.lettuce.core.codec.ByteArrayCodec
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.util.ByteUtils
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class RedisCache(redisClient: RedisClient) : CacheProvider {

    private val redisConnection = redisClient.connect(ByteArrayCodec())

    private val keySerializationPair =
        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string())

    // https://github.com/FasterXML/jackson-databind/issues/2349 to support Kotlin data classes
    private val valueSerializationPair =
        RedisSerializationContext.SerializationPair.fromSerializer(
            GenericJackson2JsonRedisSerializer(
                ObjectMapper().apply {
                    this.findAndRegisterModules()
                    this.registerKotlinModule()
                    this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    this.configure(JsonParser.Feature.IGNORE_UNDEFINED, true)
                    this.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
                    this.activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Any::class.java).build(),
                        ObjectMapper.DefaultTyping.EVERYTHING,
                        JsonTypeInfo.As.PROPERTY
                    )
                }
            )
        )

    override suspend fun add(key: Any, value: Any?) {
        redisConnection.reactive().set(serializeString(key), serializeCacheValue(value)).awaitFirstOrNull()
    }

    override suspend fun add(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?) {
        redisConnection.reactive().psetex(serializeString(key), timeUnit.toMillis(ttl), serializeCacheValue(value))
            .awaitFirstOrNull()
    }

    override suspend fun addIfAbsent(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?): Any? {
        return redisConnection.reactive()
            .set(serializeString(key), serializeCacheValue(value), SetArgs().nx().px(timeUnit.toMillis(ttl)))
            .awaitFirstOrNull().takeIf { it == "ok" }?.let { value }
    }

    override suspend fun getOrPut(key: Any, value: Any?): Any? {

        return deserializeCacheValue(
            redisConnection.reactive().getset(serializeString(key), serializeCacheValue(value)).awaitFirstOrNull()
        )
    }

    override suspend fun getOrPut(key: Any, ttl: Long, timeUnit: TimeUnit, value: Any?): Any? {
        return get(key) ?: value?.apply {
            add(key, ttl, timeUnit, this)
        }
    }

    override suspend fun get(key: Any): Any? {
        return deserializeCacheValue(redisConnection.reactive().get(serializeString(key)).awaitFirstOrNull())
    }

    override suspend fun remove(key: Any) {
        redisConnection.reactive().del(serializeString(key)).awaitFirstOrNull()
    }

    override suspend fun remove(keys: Collection<Any>) {
        batchSerializeString(keys).takeIf { it.isNotEmpty() }?.let {
            redisConnection.reactive().del(*it).awaitFirstOrNull()
        }
    }

    override suspend fun expire(key: Any, ttl: Long, timeUnit: TimeUnit) {
        redisConnection.reactive().pexpire(serializeString(key), timeUnit.toMillis(ttl)).awaitFirstOrNull()
    }

    override suspend fun incr(key: Any): Long? {
        return redisConnection.reactive().incr(serializeString(key)).awaitFirstOrNull()
    }

    private suspend fun deserializeCacheValue(value: ByteArray?): Any? {
        return value?.let {
            valueSerializationPair.read(ByteBuffer.wrap(value))
        }
    }

    private suspend fun serializeCacheValue(value: Any?): ByteArray? {
        return value?.let {
            ByteUtils.getBytes(valueSerializationPair.write(value))
        }
    }

    private fun serializeString(key: Any?): ByteArray? {
        return key?.let { k ->
            convertString(k)?.let {
                ByteUtils.getBytes(keySerializationPair.write(it))
            }
        }
    }

    private fun batchSerializeString(keys: Collection<Any>): Array<ByteArray> {
        val cacheKeys = mutableListOf<ByteArray>()
        for (key in keys) {
            convertString(key)?.let {
                cacheKeys.add(ByteUtils.getBytes(keySerializationPair.write(it)))
            }
        }
        return cacheKeys.toTypedArray()
    }

    private fun convertString(key: Any): String? {
        return if (key is String) {
            key
        } else null
    }
}
