package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto
import io.grpc.ServiceDescriptor

abstract class ServiceSupport {
    abstract val descriptor: ServiceDescriptorProto

    open val serviceDescriptor: ServiceDescriptor = ServiceDescriptor.newBuilder("").build()
}
