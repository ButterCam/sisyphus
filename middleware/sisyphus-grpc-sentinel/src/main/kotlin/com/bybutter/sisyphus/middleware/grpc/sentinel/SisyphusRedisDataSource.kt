package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.alibaba.csp.sentinel.datasource.AbstractDataSource
import com.alibaba.csp.sentinel.datasource.Converter
import com.alibaba.csp.sentinel.datasource.WritableDataSource
import com.alibaba.csp.sentinel.log.RecordLog
import com.alibaba.csp.sentinel.property.SentinelProperty
import com.alibaba.csp.sentinel.util.AssertUtil
import com.bybutter.sisyphus.jackson.toJson
import com.bybutter.sisyphus.rpc.Code
import com.bybutter.sisyphus.rpc.StatusException
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import java.util.concurrent.locks.ReentrantLock

class SisyphusRedisDataSource<T> : AbstractDataSource<String, T>, WritableDataSource<T> {

    private val lock: ReentrantLock = ReentrantLock()

    private var redisClient: RedisClient? = null

    private var key: String? = null

    private var ruleKey: String? = null

    override fun write(value: T) {
        lock.lock()
        try {
            val toJSONString = value?.toJson()
            redisClient?.connect()?.async()?.set(key, toJSONString) ?: throw StatusException(Code.UNAVAILABLE, "redisClient unAvailable")
        } finally {
            lock.unlock()
        }
    }

    /**
     * Constructor of `RedisDataSource`.
     *
     * @param connectionConfig Redis connection config
     * @param ruleKey data key in Redis
     * @param channel channel to subscribe in Redis
     * @param parser customized data parser, cannot be empty
     */
    constructor(redisClient: RedisClient, ruleKey: String, channel: String, parser: Converter<String, T>) : super(parser) {
        AssertUtil.notEmpty(ruleKey, "Redis ruleKey can not be empty")
        AssertUtil.notEmpty(channel, "Redis subscribe channel can not be empty")
        this.ruleKey = ruleKey
        this.redisClient = redisClient
        loadInitialConfig()
        subscribeFromChannel(channel)
    }

    constructor(redisClient: RedisClient, key: String, parser: Converter<String, T>) : super(parser) {
        this.redisClient = redisClient
        this.key = key
    }

    private fun subscribeFromChannel(channel: String) {
        val pubSubConnection: StatefulRedisPubSubConnection<String, String> = redisClient?.connectPubSub() ?: throw StatusException(Code.UNAVAILABLE, "redisClient unAvailable")
        val adapterListener: RedisPubSubAdapter<String, String> = DelegatingRedisPubSubListener(this.getProperty(), parser)
        pubSubConnection.addListener(adapterListener)
        val sync = pubSubConnection.sync()
        sync.subscribe(channel)
    }

    private fun loadInitialConfig() {
        try {
            val newValue = loadConfig()
            if (newValue == null) {
                RecordLog.warn("[RedisDataSource] WARN: initial config is null, you may have to check your data source")
            }
            getProperty().updateValue(newValue)
        } catch (ex: Exception) {
            RecordLog.warn("[RedisDataSource] Error when loading initial config", ex)
        }
    }

    override fun readSource(): String {
        checkNotNull(this.redisClient) { "Redis client has not been initialized or error occurred" }
        val stringRedisCommands: RedisCommands<String?, String> = redisClient?.connect()?.sync() ?: throw StatusException(Code.UNAVAILABLE, "redisClient unAvailable")
        return stringRedisCommands[ruleKey]
    }

    override fun close() {
        redisClient?.shutdown() ?: throw StatusException(Code.UNAVAILABLE, "redisClient unAvailable")
    }

    private class DelegatingRedisPubSubListener<T>(val property: SentinelProperty<T>, val parser: Converter<String, T>) : RedisPubSubAdapter<String, String>() {
        override fun message(channel: String, message: String) {
            RecordLog.info(String.format("[RedisDataSource] New property value received for channel %s: %s", channel, message))
            property.updateValue(parser.convert(message))
        }
    }
}
