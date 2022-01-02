package com.bybutter.sisyphus.middleware.grpc.proxy

import io.grpc.CallOptions

class GrpcClientProxy(
    val name: String,
    val target: String,
    val services: Set<Class<*>>,
    val options: CallOptions = CallOptions.DEFAULT
)
