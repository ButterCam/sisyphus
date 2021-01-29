package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.protobuf.primitives.MethodDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto
import io.grpc.Channel
import io.grpc.MethodDescriptor
import io.grpc.ServerMethodDefinition
import io.grpc.ServerServiceDefinition
import io.grpc.ServiceDescriptor
import org.springframework.web.server.ServerWebExchange

object TranscodingFunctions {
    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the matching path template, as a [PathTemplate].
     */
    val MATCHING_PATH_TEMPLATE_ATTRIBUTE = TranscodingFunctions::class.java.name + ".pathTemplate"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the target gRpc server channel, as a [Channel].
     */
    val GRPC_PROXY_CHANNEL_ATTRIBUTE = TranscodingFunctions::class.java.name + ".proxyChannel"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the current service definition, as a [ServerServiceDefinition].
     */
    val SERVICE_DEFINITION_ATTRIBUTE = TranscodingFunctions::class.java.name + ".serviceDefinition"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the current service descriptor, as a [ServiceDescriptor].
     */
    val SERVICE_DESCRIPTOR_ATTRIBUTE = TranscodingFunctions::class.java.name + ".serviceDescriptor"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the current service proto, as a [ServiceDescriptorProto].
     */
    val SERVICE_PROTO_ATTRIBUTE = TranscodingFunctions::class.java.name + ".serviceProto"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the current service method definition, as a [ServerMethodDefinition].
     */
    val METHOD_DEFINITION_ATTRIBUTE = TranscodingFunctions::class.java.name + ".methodDefinition"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the current service method descriptor, as a [MethodDescriptor].
     */
    val METHOD_DESCRIPTOR_ATTRIBUTE = TranscodingFunctions::class.java.name + ".methodDescriptor"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the current service method proto, as a [MethodDescriptorProto].
     */
    val METHOD_PROTO_ATTRIBUTE = TranscodingFunctions::class.java.name + ".methodProto"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the current request id, as a [String].
     */
    val REQUEST_ID_ATTRIBUTE = TranscodingFunctions::class.java.name + ".requestId"
}
