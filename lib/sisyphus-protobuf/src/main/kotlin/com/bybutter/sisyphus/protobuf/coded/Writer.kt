package com.bybutter.sisyphus.protobuf.coded

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum

@OptIn(ExperimentalUnsignedTypes::class)
interface Writer {
    fun tag(filedNumber: Int, wireType: WireType): Writer

    fun tag(value: Int): Writer

    fun int32(value: Int): Writer

    fun uint32(value: UInt): Writer

    fun sint32(value: Int): Writer

    fun fixed32(value: UInt): Writer

    fun sfixed32(value: Int): Writer

    fun int64(value: Long): Writer

    fun uint64(value: ULong): Writer

    fun sint64(value: Long): Writer

    fun fixed64(value: ULong): Writer

    fun sfixed64(value: Long): Writer

    fun float(value: Float): Writer

    fun double(value: Double): Writer

    fun bool(bool: Boolean): Writer

    fun bytes(bytes: ByteArray): Writer

    fun string(string: String): Writer

    fun enum(enum: ProtoEnum<*>): Writer

    fun message(message: Message<*, *>?): Writer

    fun any(message: Message<*, *>?): Writer

    fun beginLd(): Writer

    fun endLd(): Writer
}
