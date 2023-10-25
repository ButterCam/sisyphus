package com.bybutter.sisyphus.middleware.grpc

import io.grpc.CallOptions
import org.springframework.boot.context.properties.NestedConfigurationProperty

data class GrpcChannelProperty(
    val name: String,
    val target: String,
    val services: Set<Class<*>>,
    val tls: Boolean = false,
    val options: CallOptions = CallOptions.DEFAULT,
    val extensions: Map<String, Any> = mapOf(),
)

data class GrpcChannelProperties(
    @NestedConfigurationProperty
    val grpc: Map<String, GrpcChannelProperty>,
)
