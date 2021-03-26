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
    }
}
