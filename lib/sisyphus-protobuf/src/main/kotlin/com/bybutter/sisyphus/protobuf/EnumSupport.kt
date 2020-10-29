package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto

abstract class EnumSupport<T : ProtoEnum> : ProtoEnumDsl<T> {
    abstract val descriptor: EnumDescriptorProto
}
