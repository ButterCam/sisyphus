package com.bybutter.middleware.distributed.lock

data class RedisLockProperty(
    val redisQualifier: Class<*>
)
