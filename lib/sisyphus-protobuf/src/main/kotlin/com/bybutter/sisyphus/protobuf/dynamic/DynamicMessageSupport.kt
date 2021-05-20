package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.FileSupport
import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoSupport
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto

class DynamicMessageSupport(
    override val parent: ProtoSupport<*>,
    override val descriptor: DescriptorProto
) : MessageSupport<DynamicMessage, DynamicMessage>() {
    @InternalProtoApi
    override fun newMutable(): DynamicMessage {
        return DynamicMessage(this)
    }

    override val name: String by lazy {
        when (parent) {
            is FileSupport -> "${parent.packageName()}.${descriptor.name}"
            is MessageSupport<*, *> -> "${parent.name}.${descriptor.name}"
            else -> throw IllegalStateException("Wrong parent")
        }
    }

    private val children: Array<ProtoSupport<*>> = run {
        val messages = descriptor.nestedType.map {
            DynamicMessageSupport(this, it)
        }
        val enums = descriptor.enumType.map {
            DynamicEnumSupport(this, it)
        }
        (messages + enums).toTypedArray()
    }

    override fun children(): Array<ProtoSupport<*>> {
        return children
    }
}
