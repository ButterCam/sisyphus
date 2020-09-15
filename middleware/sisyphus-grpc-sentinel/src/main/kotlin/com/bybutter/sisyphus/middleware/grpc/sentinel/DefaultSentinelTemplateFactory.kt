package com.bybutter.sisyphus.middleware.grpc.sentinel

class DefaultSentinelTemplateFactory : SentinelTemplateFactory {

    override fun createSentinelGrpcServerInterceptor(fallbackMessage: String): SisyphusSentinelGrpcServerInterceptor {
        return SisyphusSentinelGrpcServerInterceptor(fallbackMessage)
    }
}
