package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.authentication

import org.springframework.web.reactive.function.server.ServerRequest

interface SwaggerValidate {
    fun validate(request: ServerRequest, validateContent: Map<String, String>? = null)
}
