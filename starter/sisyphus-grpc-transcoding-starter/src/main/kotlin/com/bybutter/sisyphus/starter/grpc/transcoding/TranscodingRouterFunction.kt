package com.bybutter.sisyphus.starter.grpc.transcoding

import io.grpc.ManagedChannelBuilder
import io.grpc.Server
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
class TranscodingRouterFunction private constructor(
    private val server: Server,
    private val serviceRouters: List<RouterFunction<ServerResponse>>
) : RouterFunction<ServerResponse> {

    private val channel by lazy {
        // Create channel for localhost gRpc server.
        // We create channel lazily, because get server port will cause exceptions before server started.
        ManagedChannelBuilder.forTarget("localhost:${server.port}").usePlaintext().maxInboundMetadataSize(1024 * 1024).build()
    }

    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        val oldAttributes = request.attributes().toMap()

        // Set the channel attribute for creating gRpc proxy calls.
        request.attributes()[TranscodingFunctions.GRPC_PROXY_CHANNEL_ATTRIBUTE] = channel

        // TODO: For performance considerations, we just found the first matched router,
        //  maybe we should choose the longest template for multiple matched.
        return Flux.fromIterable(serviceRouters).concatMap {
            it.route(request)
        }.next().switchIfEmpty(
            Mono.create<HandlerFunction<ServerResponse>> {
                // Clear the attributes if we can't transcode current http request to gRpc.
                request.attributes().clear()
                request.attributes() += oldAttributes
                it.success()
            }
        )
    }

    companion object {
        operator fun invoke(server: Server, enableServices: Collection<String> = listOf()): RouterFunction<ServerResponse> {
            val enableServices = enableServices.toSet()
            val serviceRouters = server.services.mapNotNull {
                if (enableServices.isEmpty() || enableServices.contains(it.serviceDescriptor.name)) {
                    TranscodingServiceRouterFunction(it)
                } else {
                    null
                }
            }
            // Return empty router if no service routers created.
            if (serviceRouters.isEmpty()) return EmptyRouterFunction

            return TranscodingRouterFunction(server, serviceRouters)
        }
    }
}
