package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.ProtoEnum

class DynamicEnum(
    override val number: Int,
    override val proto: String,
    private val support: DynamicEnumSupport
) : ProtoEnum<DynamicEnum> {
    override fun support(): EnumSupport<DynamicEnum> {
        return support
    }
}
