package com.bybutter.sisyphus.middleware.grpc.property

import io.grpc.CallOptions

data class GrpcChannelProperty(
    val name: String,
    val target: String,
    val services: Set<Class<*>>,
    val options: CallOptions = CallOptions.DEFAULT
)
