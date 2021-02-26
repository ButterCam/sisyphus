package com.bybutter.sisyphus.starter.protobuf.type

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.toEnum
import com.bybutter.sisyphus.protobuf.primitives.toType
import com.bybutter.sisyphus.starter.webflux.DetectBodyInserter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class TypeReflectionFunction : RouterFunction<ServerResponse>, HandlerFunction<ServerResponse> {
    private val typeCache = mutableMapOf<String, Any?>()

    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        if (request.method() != HttpMethod.GET) return Mono.empty()
        val typeName = ".${request.path()}"
        val type = typeCache.getOrPut(request.path()) {
            when (val descriptor = ProtoTypes.findSupport(typeName)?.descriptor) {
                is DescriptorProto -> descriptor.toType(typeName)
                is EnumDescriptorProto -> descriptor.toEnum(typeName)
                else -> null
            }
        } ?: return Mono.empty()
        request.attributes()[PROTOBUF_TYPE_ATTRIBUTE] = type
        return Mono.just(this)
    }

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val descriptor = request.attribute(PROTOBUF_TYPE_ATTRIBUTE).get() as Message<*, *>
        return ServerResponse.status(HttpStatus.OK).body(DetectBodyInserter(descriptor))
    }

    companion object {
        val PROTOBUF_TYPE_ATTRIBUTE = TypeReflectionFunction::class.java.name + ".type"
    }
}
