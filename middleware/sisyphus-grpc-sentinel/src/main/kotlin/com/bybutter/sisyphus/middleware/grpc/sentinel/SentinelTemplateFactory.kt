package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.bybutter.sisyphus.starter.grpc.SisyphusSentinelGrpcServerInterceptor

interface SentinelTemplateFactory {
    fun createSentinelGrpcServerInterceptor(fallbackMessage: String): SisyphusSentinelGrpcServerInterceptor
}
