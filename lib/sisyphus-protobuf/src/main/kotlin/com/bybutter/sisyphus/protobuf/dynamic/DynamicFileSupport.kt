package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.FileSupport
import com.bybutter.sisyphus.protobuf.ProtoSupport
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto

class DynamicFileSupport(
    override val descriptor: FileDescriptorProto,
) : FileSupport() {
    override val name: String
        get() = descriptor.name

    private val children: Array<ProtoSupport<*>> =
        run {
            val messages =
                descriptor.messageType.map {
                    DynamicMessageSupport(this, it)
                }
            val enums =
                descriptor.enumType.map {
                    DynamicEnumSupport(this, it)
                }
            val services =
                descriptor.service.map {
                    DynamicServiceSupport(this, it)
                }
            (messages + enums + services).toTypedArray()
        }

    override fun children(): Array<ProtoSupport<*>> {
        return children
    }
}
