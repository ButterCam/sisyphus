package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessagePatcher
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.findEnumSupport
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.reflect.uncheckedCast
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.Metadata
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class TranscodingHttpRouterFunction constructor(
    private val rule: TranscodingRouterRule
) : RouterFunction<ServerResponse>, HandlerFunction<ServerResponse> {
    private val requestPredicate = HttpRulePredicate(rule.http)
    private val inputSupport: MessageSupport<*, *> = ProtoTypes.findMessageSupport(rule.methodProto.inputType)
    private val bodyClass: Class<*>?

    init {
        bodyClass = when (rule.http.body) {
            "" -> null
            "*" -> ProtoTypes.findMessageSupport(rule.methodProto.inputType).messageClass.java
            else -> {
                val field = inputSupport.fieldDescriptors.firstOrNull { it.name == rule.http.body }
                    ?: throw IllegalStateException("Wrong http rule options, input message not contains body field '${rule.http.body}'.")
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
                    FieldDescriptorProto.Type.ENUM -> ProtoTypes.findEnumSupport(field.typeName).enumClass.java
                    FieldDescriptorProto.Type.MESSAGE -> ProtoTypes.findMessageSupport(field.typeName).messageClass.java
                    else -> TODO()
                }
            }
        }
    }

    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        val serviceName = request.headers().firstHeader(GRPC_SERVICE_NAME_HEADER)
        if (!serviceName.isNullOrEmpty() && rule.service.serviceDescriptor.name != serviceName) {
            return Mono.empty()
        }

        // Set the method attributes
        request.attributes()[TranscodingFunctions.TRANSCODING_RULE_ATTRIBUTE] = rule

        return if (requestPredicate.test(request)) {
            Mono.just(this)
        } else {
            Mono.empty()
        }
    }

    @OptIn(InternalProtoApi::class)
    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val channel = request.attributes()[TranscodingFunctions.GRPC_PROXY_CHANNEL_ATTRIBUTE] as Channel
        val call = channel.newCall(rule.method.methodDescriptor, CallOptions.DEFAULT)
            .uncheckedCast<ClientCall<Message<*, *>, Message<*, *>>>()
        val header = prepareHeader(request)
        val listener = TranscodingCallListener(rule.http.responseBody)

        call.start(listener, header)
        // gRPC transcoding only support the unary calls, so we request 2 message for response.
        // Server will try to return 2 messages, but just 1 message returned, so server will return
        // once and told client there is no more messages, call will be closed.
        call.request(2)

        return request.bodyToMono(bodyClass).map {
            when (rule.http.body) {
                "*" -> it as MutableMessage<*, *>
                else -> {
                    // Create request message partly with HTTP request body.
                    inputSupport.newMutable().apply {
                        this[rule.http.body] = it
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
        }.doOnError {
            logger.error("Transcoding error", it)
        }
    }

    private fun prepareHeader(request: ServerRequest): Metadata {
        val header = Metadata()
        val exporters =
            request.attributes()[TranscodingFunctions.HEADER_EXPORTER_ATTRIBUTE] as? List<TranscodingHeaderExporter>
                ?: listOf()

        for (exporter in exporters) {
            exporter.export(request, header)
        }
        return header
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TranscodingHttpRouterFunction::class.java)

        const val GRPC_SERVICE_NAME_HEADER = "grpc-service-name"
    }
}
