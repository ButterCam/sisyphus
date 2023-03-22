package com.bybutter.sisyphus.starter.grpc.openapi

import org.springframework.web.reactive.function.server.ServerRequest

interface ApiDocRequestInterceptor {
    fun intercept(request: ServerRequest)
}
