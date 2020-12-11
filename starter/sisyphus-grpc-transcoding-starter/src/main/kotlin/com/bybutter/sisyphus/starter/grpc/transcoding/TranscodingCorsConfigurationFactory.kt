package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import io.grpc.ServerMethodDefinition
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration

interface TranscodingCorsConfigurationFactory {
    fun getConfiguration(method: ServerMethodDefinition<*, *>, pattern: HttpRule.Pattern<*>, path: String): CorsConfiguration

    object Default : TranscodingCorsConfigurationFactory {
        override fun getConfiguration(method: ServerMethodDefinition<*, *>, pattern: HttpRule.Pattern<*>, path: String): CorsConfiguration {
            return CorsConfiguration().apply {
                addAllowedHeader(CorsConfiguration.ALL)
                addAllowedOrigin(CorsConfiguration.ALL)
                addAllowedMethod(HttpMethod.OPTIONS)
                addAllowedMethod(HttpMethod.HEAD)
                addExposedHeader("X-Request-Id")
            }
        }
    }
}
