package com.bybutter.sisyphus.rpc

import io.grpc.CallOptions
import io.grpc.MethodDescriptor

interface CallOptionsInterceptor {
    fun intercept(method: MethodDescriptor<*, *>, options: CallOptions): CallOptions
}
