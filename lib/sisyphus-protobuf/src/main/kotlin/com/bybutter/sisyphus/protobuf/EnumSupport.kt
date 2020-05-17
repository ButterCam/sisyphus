package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto
import io.grpc.Metadata

abstract class EnumSupport<T : ProtoEnum> : ProtoEnumDsl<T>, Metadata.AsciiMarshaller<T> {
    abstract val descriptor: EnumDescriptorProto

    override fun toAsciiString(value: T): String {
        return value.proto
    }

    override fun parseAsciiString(serialized: String): T {
        return invoke(serialized)
    }
}
