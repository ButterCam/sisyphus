package com.bybutter.sisyphus.starter.protobuf.type

import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

class SisyphusProtobufTypeServerAutoConfiguration {
    @Bean
    fun protobufTypeReflectionFunction(): RouterFunction<ServerResponse> {
        return TypeReflectionFunction()
    }
}
