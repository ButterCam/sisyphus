package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto

abstract class ServiceSupport {
    abstract val descriptor: ServiceDescriptorProto
}
