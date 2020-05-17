package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.metadata
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto
import io.grpc.ServerServiceDefinition
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Router for gRpc service.
 */
class TranscodingServiceRouterFunction private constructor(
    private val service: ServerServiceDefinition,
    private val proto: ServiceDescriptorProto,
    private val methodRouters: List<RouterFunction<ServerResponse>>
) : RouterFunction<ServerResponse> {
    private val hosts = proto.options?.metadata?.hosts?.toSet() ?: setOf()

    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        // Set the service attributes.
        request.attributes()[TranscodingFunctions.SERVICE_DEFINITION_ATTRIBUTE] = service
        request.attributes()[TranscodingFunctions.SERVICE_DESCRIPTOR_ATTRIBUTE] = service.serviceDescriptor
        request.attributes()[TranscodingFunctions.SERVICE_PROTO_ATTRIBUTE] = proto

        val host = request.headers().firstHeader(apiDomainHeader) ?: buildString {
            append(request.uri().host)
            if (request.uri().port != -1) {
                append(":")
                append(request.uri().port)
            }
        }
        if (hosts.isNotEmpty() && host !in hosts) {
            return Mono.empty()
        }

        // Find the first matched method router routed result.
        return Flux.fromIterable(methodRouters).concatMap {
            it.route(request)
        }.next()
    }

    companion object {
        val apiDomainHeader = "X-Api-Domain"

        operator fun invoke(service: ServerServiceDefinition): RouterFunction<ServerResponse>? {
            // Ensure for the service proto.
            val proto = ProtoTypes.getDescriptorBySymbol(service.serviceDescriptor.name) as? ServiceDescriptorProto
                    ?: return null
            val methodRouters = service.methods.mapNotNull { TranscodingMethodRouterFunction(it) }
            // Return null if no method routers created.
            if (methodRouters.isEmpty()) return null
            return TranscodingServiceRouterFunction(service, proto, methodRouters)
        }
    }
}
