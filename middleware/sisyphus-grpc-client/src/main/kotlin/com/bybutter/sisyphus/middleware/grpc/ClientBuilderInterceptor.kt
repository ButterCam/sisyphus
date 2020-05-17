package com.bybutter.sisyphus.middleware.grpc

import io.grpc.stub.AbstractStub

interface ClientBuilderInterceptor {
    fun intercept(stub: AbstractStub<*>): AbstractStub<*>
}
