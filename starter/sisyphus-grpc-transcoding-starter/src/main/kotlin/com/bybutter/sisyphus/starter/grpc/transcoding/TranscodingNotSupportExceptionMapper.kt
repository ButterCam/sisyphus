package com.bybutter.sisyphus.starter.grpc.transcoding

import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono

@Component
@Order(-1)
class TranscodingNotSupportExceptionMapper : WebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        // org.springframework.web.reactive.DispatcherHandler will throw ResponseStatusException with reason 'No matching handler'
        // org.springframework.web.reactive.resource.ResourceWebHandler will throw ResponseStatusException with no reason
        // We map those ResponseStatusException to TranscodingNotSupportException
        if (ex is ResponseStatusException && ex.status == HttpStatus.NOT_FOUND &&
            (ex.reason == null || ex.reason == "No matching handler")) {
            return Mono.error(TranscodingNotSupportException())
        }
        return Mono.error(ex)
    }
}
