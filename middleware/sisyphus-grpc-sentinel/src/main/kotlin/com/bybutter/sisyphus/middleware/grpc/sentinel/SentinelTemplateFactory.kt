package com.bybutter.sisyphus.middleware.grpc.sentinel

interface SentinelTemplateFactory {
    fun createSentinelGrpcServerInterceptor(fallbackMessage: String): SisyphusSentinelGrpcServerInterceptor
}
