package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.alibaba.csp.sentinel.datasource.AbstractDataSource
import com.alibaba.csp.sentinel.datasource.Converter
import com.alibaba.csp.sentinel.datasource.WritableDataSource
import com.alibaba.csp.sentinel.log.RecordLog
import com.alibaba.csp.sentinel.property.SentinelProperty
import com.alibaba.csp.sentinel.util.AssertUtil
import com.bybutter.sisyphus.jackson.toJson
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import java.util.concurrent.locks.ReentrantLock

class SisyphusRedisDataSource<T>(private val redisClient: RedisClient, parser: Converter<String, T>) : AbstractDataSource<String, T>(parser), WritableDataSource<T> {

    private val lock: ReentrantLock = ReentrantLock()

    private var key: String? = null

    private var ruleKey: String? = null

    override fun write(value: T) {
        lock.lock()
        try {
            val toJSONString = value?.toJson()
            redisClient.connect().async().set(key, toJSONString)
        } finally {
            lock.unlock()
        }
    }

    /**
     * Constructor of `RedisDataSource`.
     *
     * @param redisClient Redis client
     * @param ruleKey data key in Redis
     * @param channel channel to subscribe in Redis
     * @param parser customized data parser, cannot be empty
     */
    constructor(redisClient: RedisClient, ruleKey: String, channel: String, parser: Converter<String, T>) : this(redisClient, parser) {
        AssertUtil.notEmpty(ruleKey, "Redis ruleKey can not be empty")
        AssertUtil.notEmpty(channel, "Redis subscribe channel can not be empty")
        this.ruleKey = ruleKey
        loadInitialConfig()
        subscribeFromChannel(channel)
    }

    constructor(redisClient: RedisClient, key: String, parser: Converter<String, T>) : this(redisClient, parser) {
        this.key = key
    }

    private fun subscribeFromChannel(channel: String) {
        val pubSubConnection: StatefulRedisPubSubConnection<String, String> = redisClient.connectPubSub()
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
        val stringRedisCommands: RedisCommands<String?, String> = redisClient.connect().sync()

        return stringRedisCommands[ruleKey]
    }

    override fun close() {
        redisClient.shutdown()
    }

    private class DelegatingRedisPubSubListener<T>(val property: SentinelProperty<T>, val parser: Converter<String, T>) : RedisPubSubAdapter<String, String>() {
        override fun message(channel: String, message: String) {
            RecordLog.info(String.format("[RedisDataSource] New property value received for channel %s: %s", channel, message))
            property.updateValue(parser.convert(message))
        }
    }
}
