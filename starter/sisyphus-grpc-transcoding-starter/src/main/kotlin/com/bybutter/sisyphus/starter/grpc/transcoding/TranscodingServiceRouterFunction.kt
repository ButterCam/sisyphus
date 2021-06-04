package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.findServiceSupport
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

    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        // Set the service attributes.
        request.attributes()[TranscodingFunctions.SERVICE_DEFINITION_ATTRIBUTE] = service
        request.attributes()[TranscodingFunctions.SERVICE_DESCRIPTOR_ATTRIBUTE] = service.serviceDescriptor
        request.attributes()[TranscodingFunctions.SERVICE_PROTO_ATTRIBUTE] = proto

        val serviceName = request.headers().firstHeader(GRPC_SERVICE_NAME_HEADER)
        if (!serviceName.isNullOrEmpty() && service.serviceDescriptor.name != serviceName) {
            return Mono.empty()
        }

        // Find the first matched method router routed result.
        return Flux.fromIterable(methodRouters).concatMap {
            it.route(request)
        }.next()
    }

    companion object {
        const val GRPC_SERVICE_NAME_HEADER = "grpc-service-name"

        operator fun invoke(service: ServerServiceDefinition): RouterFunction<ServerResponse>? {
            // Ensure for the service proto.
            val proto = ProtoTypes.findServiceSupport(".${service.serviceDescriptor.name}").descriptor
            val methodRouters = service.methods.mapNotNull { TranscodingMethodRouterFunction(it) }
            // Return null if no method routers created.
            if (methodRouters.isEmpty()) return null
            return TranscodingServiceRouterFunction(service, proto, methodRouters)
        }
    }
}
