package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.alibaba.csp.sentinel.datasource.WritableDataSource
import com.alibaba.fastjson.JSON
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import java.time.Duration
import java.util.concurrent.locks.ReentrantLock

class RedisWritableDataSource<T>(host: String, port: Int, private val key: String) : WritableDataSource<T> {

    private val lock: ReentrantLock = ReentrantLock()

    private val redisClient: RedisClient = RedisClient.create(RedisURI(host, port, Duration.ofSeconds(2L)))

    override fun write(value: T) {
        lock.lock()
        try {
            val toJSONString = JSON.toJSONString(value)
            redisClient.connect().async().set(key, toJSONString)
        } finally {
            lock.unlock()
        }
    }

    override fun close() {
        redisClient.shutdown()
    }
}
