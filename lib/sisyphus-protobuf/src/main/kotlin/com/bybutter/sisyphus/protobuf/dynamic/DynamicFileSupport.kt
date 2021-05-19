package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.FileSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto

class DynamicFileSupport(override val descriptor: FileDescriptorProto) : FileSupport() {
    override val name: String
        get() = descriptor.name

    override fun register() {
        descriptor.messageType.forEach {
            ProtoTypes.register(DynamicMessageSupport(this, it))
        }
        descriptor.enumType.forEach {
            ProtoTypes.register(DynamicEnumSupport(this, it))
        }
    }
}

