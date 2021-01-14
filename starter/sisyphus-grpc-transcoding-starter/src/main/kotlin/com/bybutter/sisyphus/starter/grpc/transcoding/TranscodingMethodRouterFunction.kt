package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessagePatcher
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.MethodDescriptorProto
import com.bybutter.sisyphus.reflect.uncheckedCast
import com.bybutter.sisyphus.starter.grpc.support.SisyphusGrpcServerInterceptor.Companion.REQUEST_ID_META_KEY
import com.bybutter.sisyphus.string.randomString
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerMethodDefinition
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class TranscodingMethodRouterFunction private constructor(
    private val method: ServerMethodDefinition<*, *>,
    private val proto: MethodDescriptorProto,
    private val rule: HttpRule
) : RouterFunction<ServerResponse>, HandlerFunction<ServerResponse> {
    private val requestPredicate = HttpRulePredicate(rule)
    private val inputSupport: MessageSupport<*, *> = ProtoTypes.ensureSupportByProtoName(proto.inputType)
    private val bodyClass: Class<*>?

    init {
        bodyClass = when (rule.body) {
            "" -> null
            "*" -> ProtoTypes.ensureClassByProtoName(proto.inputType)
            else -> {
                val field = inputSupport.fieldDescriptors.firstOrNull { it.name == rule.body }
                    ?: throw IllegalStateException("Wrong http rule options, input message not contains body field '${rule.body}'.")
                when (field.type) {
                    FieldDescriptorProto.Type.DOUBLE -> Double::class.java
                    FieldDescriptorProto.Type.FLOAT -> Float::class.java
                    FieldDescriptorProto.Type.SINT64,
                    FieldDescriptorProto.Type.SFIXED64,
                    FieldDescriptorProto.Type.INT64 -> Long::class.java
                    FieldDescriptorProto.Type.UINT64 -> ULong::class.java
                    FieldDescriptorProto.Type.SINT32,
                    FieldDescriptorProto.Type.SFIXED32,
                    FieldDescriptorProto.Type.INT32 -> Int::class.java
                    FieldDescriptorProto.Type.FIXED64 -> ULong::class.java
                    FieldDescriptorProto.Type.UINT32,
                    FieldDescriptorProto.Type.FIXED32 -> UInt::class.java
                    FieldDescriptorProto.Type.BOOL -> Boolean::class.java
                    FieldDescriptorProto.Type.STRING -> String::class.java
                    FieldDescriptorProto.Type.BYTES -> ByteArray::class.java
                    FieldDescriptorProto.Type.ENUM,
                    FieldDescriptorProto.Type.MESSAGE -> ProtoTypes.ensureClassByProtoName(field.typeName)
                    else -> TODO()
                }
            }
        }
    }

    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        // Set the method attributes
        request.attributes()[TranscodingFunctions.METHOD_DEFINITION_ATTRIBUTE] = method
        request.attributes()[TranscodingFunctions.METHOD_DESCRIPTOR_ATTRIBUTE] = method.methodDescriptor
        request.attributes()[TranscodingFunctions.METHOD_PROTO_ATTRIBUTE] = proto

        return if (requestPredicate.test(request)) {
            Mono.just(this)
        } else {
            Mono.empty()
        }
    }

    @OptIn(InternalProtoApi::class)
    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        // We create request id in HTTP server but not gRPC server, it due to error maybe caused before gRPC calling
        // or gRPC request id wrote, so we generate it here and set it to request attribute.
        val requestId = request.headers().header(REQUEST_ID_META_KEY.name()).firstOrNull()
                ?: randomString(12)

        request.attributes()[TranscodingFunctions.REQUEST_ID_ATTRIBUTE] = requestId

        val channel = request.attributes()[TranscodingFunctions.GRPC_PROXY_CHANNEL_ATTRIBUTE] as Channel
        val call = channel.newCall(method.methodDescriptor, CallOptions.DEFAULT).uncheckedCast<ClientCall<Message<*, *>, Message<*, *>>>()
        val header = prepareHeader(request)
        val listener = TranscodingCallListener(rule.responseBody)

        call.start(listener, header)
        // gRPC transcoding only support the unary calls, so we request 2 message for response.
        // Server will try to return 2 messages, but just 1 message returned, so server will return
        // once and told client there is no more messages, call will be closed.
        call.request(2)

        return request.bodyToMono(bodyClass).map {
            when (rule.body) {
                "*" -> it as MutableMessage<*, *>
                else -> {
                    // Create request message partly with HTTP request body.
                    inputSupport.newMutable().apply {
                        this[rule.body] = it
                    }
                }
            }
            // Create empty message when no HTTP request body provided.
        }.defaultIfEmpty(inputSupport.newMutable()).map {
            MessagePatcher().apply {
                // Add path variables to message patcher.
                addAll(request.pathVariables())
                // Add query parameters to message patcher.
                addAllList(request.queryParams())
                // Apply patcher to message.
                applyTo(it)
            }
            // Send the request message.
            call.sendMessage(it)
            // Tell server no more message.
            call.halfClose()
        }.flatMap {
            listener.response()
        }
    }

    private fun prepareHeader(request: ServerRequest): Metadata {
        val header = Metadata()
        for ((key, values) in request.headers().asHttpHeaders()) {
            header.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), values.joinToString(","))
        }
        return header
    }

    companion object {
        operator fun invoke(method: ServerMethodDefinition<*, *>): RouterFunction<ServerResponse>? {
            // Just support transcoding for unary gRPC calls.
            if (method.methodDescriptor.type != MethodDescriptor.MethodType.UNARY)
                return null
            // Ensure method proto registered.
            val proto = ProtoTypes.getDescriptorBySymbol(method.methodDescriptor.fullMethodName) as? MethodDescriptorProto
                    ?: return null
            // Ensure http rule existed.
            val httpRule = proto.options?.http
                    ?: return null

            return TranscodingMethodRouterFunction(method, proto, httpRule)
        }
    }
}
