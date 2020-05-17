package com.bybutter.sisyphus.middleware.grpc

import io.grpc.CallOptions

data class GrpcChannelProperty(
    val name: String,
    val target: String,
    val clients: Set<Class<*>>,
    val options: CallOptions = CallOptions.DEFAULT
)
