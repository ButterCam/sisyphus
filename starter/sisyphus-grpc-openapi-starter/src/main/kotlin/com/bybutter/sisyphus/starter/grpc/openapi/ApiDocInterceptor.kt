package com.bybutter.sisyphus.starter.grpc.openapi

import io.swagger.v3.oas.models.OpenAPI
import org.springframework.web.reactive.function.server.ServerRequest

interface ApiDocInterceptor {
    fun intercept(request: ServerRequest, openAPI: OpenAPI)
}
