package com.bybutter.sisyphus.middleware.grpc.sentinel

data class SentinelProperties(
    val serverAddr: String,
    val projectName: String,
    val fallbackMessage: String,
    val database: SentinelDatabase = SentinelDatabase.NONE,
    val redisClientName: String? = null
)

enum class SentinelDatabase {
    NONE, REDIS, FILE
}
