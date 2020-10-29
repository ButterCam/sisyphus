package com.bybutter.sisyphus.protobuf.compiler

import com.google.protobuf.DescriptorProtos

class MapEntryFieldGenerator(parent: MapEntryGenerator, descriptor: DescriptorProtos.FieldDescriptorProto) : FieldGenerator(parent, descriptor) {
    override val nullable = false

    override fun defaultValue(): String {
        if (descriptor.isRepeated) {
            return if (typeElement is MapEntryGenerator) {
                "mapOf()"
            } else {
                "listOf()"
            }
        }

        return when (descriptor.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE -> "0.0"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT -> "0.0f"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64 -> "0L"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64 -> "0UL"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32 -> "0"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL -> "false"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> "\"\""
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> "byteArrayOf()"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32 -> "0U"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM -> "$valueType()"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP -> throw UnsupportedOperationException("Group is not supported by butter proto.")
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE -> "$valueType()"
        }
    }
}
