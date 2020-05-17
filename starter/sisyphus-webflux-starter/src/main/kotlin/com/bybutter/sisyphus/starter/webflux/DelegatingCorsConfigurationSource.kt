package com.bybutter.sisyphus.starter.webflux

import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.server.ServerWebExchange

class DelegatingCorsConfigurationSource(sources: Iterable<CorsConfigurationSource>) : CorsConfigurationSource {
    private val sources: List<CorsConfigurationSource> = sources.toList()

    override fun getCorsConfiguration(exchange: ServerWebExchange): CorsConfiguration? {
        for (it in sources) {
            return it.getCorsConfiguration(exchange) ?: continue
        }
        return null
    }
}
