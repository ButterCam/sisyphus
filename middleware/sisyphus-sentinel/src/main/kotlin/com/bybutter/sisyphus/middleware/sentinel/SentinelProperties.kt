package com.bybutter.sisyphus.middleware.sentinel

data class SentinelProperties(
        val dashboardAddr: String,
        val projectName: String,
        val fallbackMessage: String,
        val database: SentinelDatabase = SentinelDatabase.NONE,
        val redisQualifier: Class<*>? = null
)

enum class SentinelDatabase {
    NONE, REDIS, FILE
}
