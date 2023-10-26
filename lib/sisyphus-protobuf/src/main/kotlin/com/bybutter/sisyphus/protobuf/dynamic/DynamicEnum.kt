package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.ProtoEnum

class DynamicEnum(
    override val number: Int,
    override val proto: String,
    private val support: DynamicEnumSupport,
) : ProtoEnum<DynamicEnum> {
    override fun support(): EnumSupport<DynamicEnum> {
        return support
    }

    override fun toString(): String {
        return proto
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ProtoEnum<*>) return false
        if (support.name != other.support().name) return false
        if (proto != other.proto) return false
        return true
    }
}
