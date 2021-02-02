package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import io.grpc.ServerMethodDefinition
import org.springframework.web.cors.CorsConfiguration

interface TranscodingCorsConfigurationInterceptor {
    fun intercept(
        config: CorsConfiguration,
        method: ServerMethodDefinition<*, *>,
        pattern: HttpRule.Pattern<*>,
        path: String
    ): CorsConfiguration
}
