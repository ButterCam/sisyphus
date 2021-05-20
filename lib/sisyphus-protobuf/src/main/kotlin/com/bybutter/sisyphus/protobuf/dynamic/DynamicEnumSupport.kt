package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.FileSupport
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoSupport
import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto

class DynamicEnumSupport(
    override val parent: ProtoSupport<*>,
    override val descriptor: EnumDescriptorProto
) : EnumSupport<DynamicEnum>() {
    private val values = descriptor.value.map {
        DynamicEnum(it.number, it.name, this)
    }.toTypedArray()

    override val name: String by lazy {
        when (parent) {
            is FileSupport -> "${parent.packageName()}.${descriptor.name}"
            is MessageSupport<*, *> -> "${parent.name}.${descriptor.name}"
            else -> throw IllegalStateException("Wrong parent")
        }
    }

    override fun values(): Array<DynamicEnum> {
        return values
    }
}
