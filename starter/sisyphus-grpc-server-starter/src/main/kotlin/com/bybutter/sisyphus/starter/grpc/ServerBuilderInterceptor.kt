package com.bybutter.sisyphus.starter.grpc

import io.grpc.ServerBuilder

interface ServerBuilderInterceptor {
    fun intercept(builder: ServerBuilder<*>): ServerBuilder<*>
}
