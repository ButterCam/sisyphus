package com.bybutter.middleware.distributed.lock

data class RedisLockProperty(
    val redisName: String,
    val redisQualifier: String
)
