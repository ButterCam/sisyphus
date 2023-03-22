package com.bybutter.sisyphus.starter.grpc.openapi

import com.bybutter.sisyphus.starter.webflux.EmptyRouterFunction
import io.grpc.Server
import io.grpc.ServerServiceDefinition
import io.swagger.v3.core.util.Json
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class ApiDocRouterFunction private constructor(
    private val services: List<ServerServiceDefinition>,
    private val apiDocProperty: ApiDocProperty,
    private val requestInterceptors: List<ApiDocRequestInterceptor>,
    private val interceptors: List<ApiDocInterceptor>
) : RouterFunction<ServerResponse>, HandlerFunction<ServerResponse> {

    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        return if (request.path() != apiDocProperty.path) {
            Mono.empty()
        } else {
            Mono.just(this)
        }
    }

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        requestInterceptors.forEach {
            it.intercept(request)
        }
        val openApi = openApi {
            for (service in services) {
                addService(service)
            }
        }.apply {
            interceptors.forEach {
                it.intercept(request, this)
            }
        }
        return ServerResponse.ok().bodyValue(Json.mapper().writeValueAsString(openApi))
    }

    companion object {
        const val COMPONENTS_SCHEMAS_PREFIX = "#/components/schemas/"
        operator fun invoke(
            server: Server,
            enableServices: Collection<String> = listOf(),
            apiDocProperty: ApiDocProperty,
            requestInterceptors: List<ApiDocRequestInterceptor>,
            interceptors: List<ApiDocInterceptor>
        ): RouterFunction<ServerResponse> {
            val enableServicesSet = enableServices.toSet()
            val enableServicesDefinition = mutableListOf<ServerServiceDefinition>()
            server.services.forEach {
                if (enableServicesSet.isEmpty() || enableServicesSet.contains(it.serviceDescriptor.name)) {
                    enableServicesDefinition.add(it)
                }
            }
            if (enableServicesDefinition.isEmpty()) return EmptyRouterFunction
            return ApiDocRouterFunction(enableServicesDefinition, apiDocProperty, requestInterceptors, interceptors)
        }
    }
}
