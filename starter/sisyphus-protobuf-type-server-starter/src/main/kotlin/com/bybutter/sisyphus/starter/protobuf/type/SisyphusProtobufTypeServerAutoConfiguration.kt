package com.bybutter.sisyphus.starter.protobuf.type

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class SisyphusProtobufTypeServerAutoConfiguration {
    @Bean
    fun protobufTypeReflectionFunction(): RouterFunction<ServerResponse> {
        return TypeReflectionFunction()
    }
}
