package com.bybutter.sisyphus.starter.grpc.transcoding

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class TranscodingAttributeInterceptor : WebFilter {
    @Autowired(required = false)
    private val headerExporters: List<TranscodingHeaderExporter> = listOf()

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        exchange.attributes[TranscodingFunctions.HEADER_EXPORTER_ATTRIBUTE] = headerExporters
        return chain.filter(exchange)
    }
}
