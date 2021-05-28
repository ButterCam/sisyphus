package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger

import org.springframework.web.reactive.function.server.ServerRequest

interface SwaggerAuthorizer {
    fun authorize(request: ServerRequest)
}
