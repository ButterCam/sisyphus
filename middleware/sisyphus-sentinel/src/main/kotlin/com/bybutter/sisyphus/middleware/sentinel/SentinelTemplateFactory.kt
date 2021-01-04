package com.bybutter.sisyphus.middleware.sentinel

import com.bybutter.sisyphus.middleware.sentinel.interceptor.SisyphusSentinelGrpcServerInterceptor

interface SentinelTemplateFactory {
    fun createSentinelGrpcServerInterceptor(fallbackMessage: String): SisyphusSentinelGrpcServerInterceptor
}
