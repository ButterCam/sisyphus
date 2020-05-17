package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.api.resource.PathTemplate
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.MethodDescriptorProto
import com.bybutter.sisyphus.string.PathMatcher
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
class TranscodingCorsConfigurationSource(server: Server, baseConfiguration: CorsConfiguration, enableServices: Collection<String> = listOf()) : CorsConfigurationSource {
    private val corsConfigurations = mutableMapOf<String, CorsConfiguration>()

    init {
        registerServer(server, baseConfiguration, enableServices)
    }

    override fun getCorsConfiguration(exchange: ServerWebExchange): CorsConfiguration? {
        val lookupPath = exchange.request.path.pathWithinApplication()

        // Find a supported request path.
        val pattern = corsConfigurations.keys.firstOrNull {
            PathMatcher.match(it, lookupPath.value())
        } ?: return null

        return corsConfigurations[pattern]
    }

    private fun registerServer(server: Server, baseConfiguration: CorsConfiguration, enableServices: Collection<String>) {
        val services = enableServices.toSet()

        // Resister all enable service in gRPC server.
        for (service in server.services) {
            if (services.isEmpty() || services.contains(service.serviceDescriptor.name)) {
                registerService(service, baseConfiguration)
            }
        }
    }

    private fun registerService(service: ServerServiceDefinition, baseConfiguration: CorsConfiguration) {
        // Resister all method in gRPC service.
        for (method in service.methods) {
            registerMethod(method, baseConfiguration)
        }
    }

    private fun registerMethod(method: ServerMethodDefinition<*, *>, baseConfiguration: CorsConfiguration) {
        // Ensure method proto registered.
        val proto = ProtoTypes.getDescriptorBySymbol(method.methodDescriptor.fullMethodName) as? MethodDescriptorProto
                ?: return

        // Ensure http rule existed.
        val httpRule = proto.options?.http ?: return

        registerRule(httpRule, baseConfiguration)
    }

    private fun registerRule(rule: HttpRule, baseConfiguration: CorsConfiguration) {
        rule.pattern?.let {
            registerPattern(it, baseConfiguration)
        }

        for (additionalBinding in rule.additionalBindings) {
            registerRule(rule, baseConfiguration)
        }
    }

    private fun registerPattern(pattern: HttpRule.Pattern<*>, baseConfiguration: CorsConfiguration) {
        val method: HttpMethod
        val pathTemplate: PathTemplate

        when (pattern) {
            is HttpRule.Pattern.Get -> {
                method = HttpMethod.GET
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Post -> {
                method = HttpMethod.POST
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Put -> {
                method = HttpMethod.PUT
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Patch -> {
                method = HttpMethod.PATCH
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Delete -> {
                method = HttpMethod.DELETE
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Custom -> {
                method = HttpMethod.valueOf(pattern.value.kind)
                pathTemplate = PathTemplate.create(pattern.value.path)
            }
            else -> throw UnsupportedOperationException("Unknown http rule pattern")
        }

        val normalizedPattern = pathTemplate.withoutVars().toString()

        val config = corsConfigurations.getOrPut(normalizedPattern) {
            // Create a copy of CORS configuration. The [CorsConfiguration(CorsConfiguration other)] can't be used
            // in this case, because it will refer the list instance for new config, it cause the reference leak.
            CorsConfiguration().apply {
                // Set the property will create a copy of list.
                allowedOrigins = baseConfiguration.allowedOrigins
                allowedMethods = baseConfiguration.allowedMethods
                allowedHeaders = baseConfiguration.allowedHeaders
                exposedHeaders = baseConfiguration.exposedHeaders
                allowCredentials = baseConfiguration.allowCredentials
                maxAge = baseConfiguration.maxAge
            }
        }

        config.addAllowedMethod(method)
    }
}
