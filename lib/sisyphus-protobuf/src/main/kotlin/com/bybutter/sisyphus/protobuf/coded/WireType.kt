package com.bybutter.sisyphus.protobuf.coded

import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

enum class WireType {
    VARINT,
    FIXED64,
    LENGTH_DELIMITED,
    START_GROUP,
    END_GROUP,
    FIXED32;

    companion object {
        const val TAG_TYPE_BITS = 3
        const val TAG_TYPE_MASK = (1 shl TAG_TYPE_BITS) - 1

        fun getFieldNumber(tag: Int): Int {
            return tag ushr TAG_TYPE_BITS
        }

        fun getWireTypeNumber(tag: Int): Int {
            return tag and TAG_TYPE_MASK
        }

        fun getWireType(tag: Int): WireType {
            return valueOf(getWireTypeNumber(tag))
        }

        fun valueOf(type: Int): WireType {
            return when (type) {
                0 -> VARINT
                1 -> FIXED64
                2 -> LENGTH_DELIMITED
                3 -> START_GROUP
                4 -> END_GROUP
                5 -> FIXED32
                else -> throw IllegalStateException("Invalid wire type ($type).")
            }
        }

        fun tagOf(number: Int, type: WireType): Int {
            return (number shl TAG_TYPE_BITS) or (type.ordinal and TAG_TYPE_MASK)
        }

        fun ofType(type: FieldDescriptorProto.Type): WireType {
            return when (type) {
                FieldDescriptorProto.Type.DOUBLE,
                FieldDescriptorProto.Type.FIXED64,
                FieldDescriptorProto.Type.SFIXED64 -> FIXED64

                FieldDescriptorProto.Type.FLOAT,
                FieldDescriptorProto.Type.FIXED32,
                FieldDescriptorProto.Type.SFIXED32 -> FIXED32

                FieldDescriptorProto.Type.INT64,
                FieldDescriptorProto.Type.UINT64,
                FieldDescriptorProto.Type.SINT64,
                FieldDescriptorProto.Type.INT32,
                FieldDescriptorProto.Type.UINT32,
                FieldDescriptorProto.Type.SINT32,
                FieldDescriptorProto.Type.BOOL,
                FieldDescriptorProto.Type.ENUM -> VARINT

                FieldDescriptorProto.Type.STRING,
                FieldDescriptorProto.Type.MESSAGE,
                FieldDescriptorProto.Type.BYTES -> LENGTH_DELIMITED

                FieldDescriptorProto.Type.GROUP -> TODO()
            }
        }
    }
}
