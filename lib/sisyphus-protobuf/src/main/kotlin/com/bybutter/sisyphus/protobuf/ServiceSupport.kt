package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto

abstract class ServiceSupport : ProtoSupport<ServiceDescriptorProto> {
    override val reflection: ProtoReflection
        get() = file().reflection
}
