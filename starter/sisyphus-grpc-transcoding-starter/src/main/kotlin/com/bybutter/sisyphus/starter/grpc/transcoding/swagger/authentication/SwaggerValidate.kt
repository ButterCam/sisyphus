package com.bybutter.sisyphus.starter.grpc.transcoding.swagger.authentication

import org.springframework.web.reactive.function.server.ServerRequest

interface SwaggerValidate {
    fun validate(request: ServerRequest, validateContent: Map<String, String>? = null)
}
