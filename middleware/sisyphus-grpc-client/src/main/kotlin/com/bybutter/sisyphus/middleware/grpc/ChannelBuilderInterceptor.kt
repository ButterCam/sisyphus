package com.bybutter.sisyphus.middleware.grpc

import io.grpc.ManagedChannelBuilder

interface ChannelBuilderInterceptor {
    fun intercept(builder: ManagedChannelBuilder<*>): ManagedChannelBuilder<*>
}
