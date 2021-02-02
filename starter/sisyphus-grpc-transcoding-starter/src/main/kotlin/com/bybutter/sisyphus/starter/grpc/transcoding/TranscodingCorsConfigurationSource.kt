package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.ServiceSupport
import com.bybutter.sisyphus.string.PathMatcher
import com.google.api.pathtemplate.PathTemplate
import io.grpc.Server
import io.grpc.ServerMethodDefinition
import io.grpc.ServerServiceDefinition
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.server.ServerWebExchange

/**
 * CORS config source for gRPC transcoding.
 */
class TranscodingCorsConfigurationSource(
    server: Server,
    private val configurationFactory: TranscodingCorsConfigurationFactory,
    enableServices: Collection<String> = listOf()
) : CorsConfigurationSource {
    private val corsConfigurations = mutableMapOf<String, CorsConfiguration>()

    init {
        registerServer(server, enableServices)
    }

    override fun getCorsConfiguration(exchange: ServerWebExchange): CorsConfiguration? {
        val lookupPath = exchange.request.path.pathWithinApplication()

        // Find a supported request path.
        val pattern = corsConfigurations.keys.firstOrNull {
            PathMatcher.match(it, lookupPath.value(), setOf('/', ':'))
        } ?: return null

        return corsConfigurations[pattern]
    }

    private fun registerServer(server: Server, enableServices: Collection<String>) {
        val services = enableServices.toSet()

        // Resister all enable service in gRPC server.
        for (service in server.services) {
            if (services.isEmpty() || services.contains(service.serviceDescriptor.name)) {
                registerService(service)
            }
        }
    }

    private fun registerService(service: ServerServiceDefinition) {
        // Resister all method in gRPC service.
        for (method in service.methods) {
            registerMethod(method)
        }
    }

    private fun registerMethod(method: ServerMethodDefinition<*, *>) {
        // Ensure method proto registered.
        val serice = method.methodDescriptor.serviceName?.let { ProtoTypes.findSupport(it) } as? ServiceSupport
            ?: return
        val proto = serice.descriptor.method.firstOrNull {
            it.name == method.methodDescriptor.fullMethodName.substringAfter('/')
        }

        // Ensure http rule existed.
        val httpRule = proto?.options?.http ?: return

        registerRule(httpRule, method)
    }

    private fun registerRule(rule: HttpRule, method: ServerMethodDefinition<*, *>) {
        rule.pattern?.let {
            registerPattern(it, method)
        }

        for (additionalBinding in rule.additionalBindings) {
            registerRule(rule, method)
        }
    }

    private fun registerPattern(pattern: HttpRule.Pattern<*>, method: ServerMethodDefinition<*, *>) {
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

        val config = corsConfigurations.getOrPut(normalizedPattern) {
            configurationFactory.getConfiguration(method, pattern, normalizedPattern)
        }

        config.addAllowedMethod(httpMethod)
    }
}
