package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.FileSupport
import com.bybutter.sisyphus.protobuf.ProtoSupport
import com.bybutter.sisyphus.protobuf.ServiceSupport
import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto

class DynamicServiceSupport(
    override val parent: ProtoSupport<*>,
    override val descriptor: ServiceDescriptorProto
) : ServiceSupport() {
    override val name: String by lazy {
        when (parent) {
            is FileSupport -> "${parent.packageName()}.${descriptor.name}"
            else -> throw IllegalStateException("Wrong parent")
        }
    }
}
