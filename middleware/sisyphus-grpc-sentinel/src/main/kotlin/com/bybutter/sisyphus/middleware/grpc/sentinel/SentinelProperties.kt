package com.bybutter.sisyphus.middleware.grpc.sentinel

data class SentinelProperties(
    val serverAddr: String,
    val projectName: String,
    val fallbackMessage: String,
    val redisHost: String,
    val redisPort: Int
)
