package com.bybutter.sisyphus.middleware.sentinel

import com.bybutter.sisyphus.middleware.sentinel.interceptor.SisyphusSentinelGrpcServerInterceptor

class DefaultSentinelTemplateFactory : SentinelTemplateFactory {

    override fun createSentinelGrpcServerInterceptor(fallbackMessage: String): SisyphusSentinelGrpcServerInterceptor {
        return SisyphusSentinelGrpcServerInterceptor(fallbackMessage)
    }
}
