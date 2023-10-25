package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.starter.webflux.EmptyRouterFunction
import io.grpc.Channel
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Http request router based on Spring webflux function,
 * provided Http+Json to gRpc+protobuf transcoding.
 */
class TranscodingRouterFunction constructor(
    private val serviceRouters: List<RouterFunction<ServerResponse>>,
    private val transcodingChannel: Channel,
) : RouterFunction<ServerResponse> {
    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        val oldAttributes = request.attributes().toMap()

        // Set the channel attribute for creating gRPC proxy calls.
        request.attributes()[TranscodingFunctions.GRPC_PROXY_CHANNEL_ATTRIBUTE] = transcodingChannel

        // TODO: For performance considerations, we just found the first matched router,
        //  maybe we should choose the longest template for multiple matched.
        return Flux.fromIterable(serviceRouters).concatMap {
            it.route(request)
        }.next().switchIfEmpty(
            Mono.create<HandlerFunction<ServerResponse>> {
                // Clear the attributes if we can't transcode current http request to gRPC.
                request.attributes().clear()
                request.attributes() += oldAttributes
                it.success()
            },
        )
    }

    companion object {
        operator fun invoke(
            rules: Collection<TranscodingRouterRule>,
            transcodingChannel: Channel,
        ): RouterFunction<ServerResponse> {
            // Return empty router if no service routers created.
            if (rules.isEmpty()) return EmptyRouterFunction

            return TranscodingRouterFunction(
                rules.map { TranscodingHttpRouterFunction(it) },
                transcodingChannel,
            )
        }
    }
}
