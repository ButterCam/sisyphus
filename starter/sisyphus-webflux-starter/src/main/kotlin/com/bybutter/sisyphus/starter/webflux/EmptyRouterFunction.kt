package com.bybutter.sisyphus.starter.webflux

import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

/**
 * Empty router just return empty mono.
 */
object EmptyRouterFunction : RouterFunction<ServerResponse> {
    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        return Mono.empty()
    }
}
