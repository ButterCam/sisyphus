package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.authentication

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.ServerRequest

class DefaultSwaggerValidate : SwaggerValidate {
    override fun validate(request: ServerRequest, validateContent: Map<String, String>?) {
        for ((key, value) in validateContent ?: mapOf()) {
            if (!request.headers().header(key).contains(value)) {
                throw IllegalArgumentException("Authentication failed.")
            }
        }
    }
}

class DefaultSwaggerConfig {
    @Bean
    @ConditionalOnMissingBean(value = [SwaggerValidate::class])
    fun defaultSwaggerAuthentication(): SwaggerValidate {
        return DefaultSwaggerValidate()
    }
}
