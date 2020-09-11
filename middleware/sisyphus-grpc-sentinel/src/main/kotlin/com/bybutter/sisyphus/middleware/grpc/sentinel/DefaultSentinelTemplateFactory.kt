package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.bybutter.sisyphus.starter.grpc.SisyphusSentinelGrpcServerInterceptor

class DefaultSentinelTemplateFactory : SentinelTemplateFactory {

    override fun createSentinelGrpcServerInterceptor(fallbackMessage: String): SisyphusSentinelGrpcServerInterceptor {
        return SisyphusSentinelGrpcServerInterceptor(fallbackMessage)
    }
}
