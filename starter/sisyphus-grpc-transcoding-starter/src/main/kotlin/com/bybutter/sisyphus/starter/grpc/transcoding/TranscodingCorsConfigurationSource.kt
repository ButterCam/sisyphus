package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.string.PathMatcher
import com.google.api.pathtemplate.PathTemplate
import io.grpc.ServerMethodDefinition
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.server.ServerWebExchange

/**
 * CORS config source for gRPC transcoding.
 */
class TranscodingCorsConfigurationSource(
    private val rules: Collection<TranscodingRouterRule>,
    private val interceptors: Iterable<TranscodingCorsConfigurationInterceptor>,
) : CorsConfigurationSource {
    private val corsConfigurations = mutableMapOf<String, CorsConfiguration>()

    init {
        for (rule in rules) {
            registerRule(rule)
        }
    }

    override fun getCorsConfiguration(exchange: ServerWebExchange): CorsConfiguration? {
        val lookupPath = exchange.request.path.pathWithinApplication()

        // Find a supported request path.
        val pattern =
            corsConfigurations.keys.firstOrNull {
                PathMatcher.match(it, lookupPath.value().substring(1), setOf('/', ':'))
            } ?: return null

        return corsConfigurations[pattern]
    }

    private fun registerRule(rule: TranscodingRouterRule) {
        registerHttp(rule.http, rule.method)
    }

    private fun registerHttp(
        http: HttpRule,
        method: ServerMethodDefinition<*, *>,
    ) {
        http.pattern?.let {
            registerPattern(it, method)
        }

        for (additionalBinding in http.additionalBindings) {
            registerHttp(additionalBinding, method)
        }
    }

    private fun registerPattern(
        pattern: HttpRule.Pattern<*>,
        method: ServerMethodDefinition<*, *>,
    ) {
        val httpMethod: HttpMethod
        val pathTemplate: PathTemplate

        when (pattern) {
            is HttpRule.Pattern.Get -> {
                httpMethod = HttpMethod.GET
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Post -> {
                httpMethod = HttpMethod.POST
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Put -> {
                httpMethod = HttpMethod.PUT
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Patch -> {
                httpMethod = HttpMethod.PATCH
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Delete -> {
                httpMethod = HttpMethod.DELETE
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Custom -> {
                httpMethod = HttpMethod.valueOf(pattern.value.kind)
                pathTemplate = PathTemplate.create(pattern.value.path)
            }
            else -> throw UnsupportedOperationException("Unknown http rule pattern")
        }

        val normalizedPattern = pathTemplate.withoutVars().toString()

        val config =
            corsConfigurations.getOrPut(normalizedPattern) {
                interceptors.fold(
                    CorsConfiguration().apply {
                        addAllowedHeader(CorsConfiguration.ALL)
                        addAllowedOrigin(CorsConfiguration.ALL)
                        addAllowedMethod(HttpMethod.OPTIONS)
                        addAllowedMethod(HttpMethod.HEAD)
                    },
                ) { config, interceptor ->
                    interceptor.intercept(config, method, pattern, normalizedPattern)
                }
            }

        config.addAllowedMethod(httpMethod)
    }
}
