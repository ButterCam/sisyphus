package com.bybutter.sisyphus.protobuf.coded

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
            return WireType.values().getOrNull(type)
                ?: throw IllegalStateException("Invalid wire type ($type).")
        }

        fun tagOf(number: Int, type: WireType): Int {
            return (number shl TAG_TYPE_BITS) or (type.ordinal and TAG_TYPE_MASK)
        }
    }
}
