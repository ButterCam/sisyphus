package com.bybutter.sisyphus.protobuf.coded

import com.bybutter.sisyphus.data.encodeZigZag
import com.bybutter.sisyphus.data.varintSize
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.primitives.Any

@OptIn(ExperimentalUnsignedTypes::class)
class MeasureWriter : Writer {
    private val root: LdMark = LdMark()
    private var mark: LdMark = root

    fun mark(): LdMark {
        return root
    }

    fun length(): Int {
        return root.length()
    }

    private fun append(length: Int): MeasureWriter {
        mark.append(length)
        return this
    }

    override fun tag(filedNumber: Int, wireType: WireType): MeasureWriter {
        return tag(WireType.tagOf(filedNumber, wireType))
    }

    override fun tag(value: Int): MeasureWriter {
        return int32(value)
    }

    override fun int32(value: Int): MeasureWriter {
        return append(value.varintSize)
    }

    override fun uint32(value: UInt): MeasureWriter {

        return append(value.toInt().varintSize)
    }

    override fun sint32(value: Int): MeasureWriter {

        return append(value.encodeZigZag().varintSize)
    }

    override fun fixed32(value: UInt): MeasureWriter {

        return append(4)
    }

    override fun sfixed32(value: Int): MeasureWriter {

        return append(4)
    }

    override fun int64(value: Long): MeasureWriter {

        return append(value.varintSize)
    }

    override fun uint64(value: ULong): MeasureWriter {

        return append(value.toLong().varintSize)
    }

    override fun sint64(value: Long): MeasureWriter {

        return append(value.encodeZigZag().varintSize)
    }

    override fun fixed64(value: ULong): MeasureWriter {

        return append(8)
    }

    override fun sfixed64(value: Long): MeasureWriter {

        return append(8)
    }

    override fun float(value: Float): MeasureWriter {

        return append(4)
    }

    override fun double(value: Double): MeasureWriter {

        return append(8)
    }

    override fun bool(bool: Boolean): MeasureWriter {
        return int32(if (bool) 1 else 0)
    }

    override fun bytes(bytes: ByteArray): MeasureWriter {
        append(bytes.size.varintSize)
        return append(bytes.size)
    }

    override fun string(string: String): MeasureWriter {
        return bytes(string.toByteArray())
    }

    override fun enum(enum: ProtoEnum): MeasureWriter {
        return int32(enum.number)
    }

    override fun message(message: Message<*, *>?): MeasureWriter {
        message ?: return this
        val writer = beginLd()
        message.writeTo(writer)
        return writer.endLd()
    }

    override fun any(message: Message<*, *>?): MeasureWriter {
        message ?: return this
        if (message is Any) {
            return message(message)
        }
        return beginLd()
            .append(1).string(message.typeUrl())
            .append(1).message(message)
            .endLd()
    }

    override fun beginLd(): MeasureWriter {
        mark = mark.enter()
        return this
    }

    override fun endLd(): MeasureWriter {
        mark = mark.exit()
        return this
    }

    class LdMark private constructor(
        private val parent: LdMark? = null,
        private val list: MutableList<LdMark> = parent?.list ?: mutableListOf(),
    ) {
        constructor() : this(null)

        private var length: Int = 0

        fun length() = length

        fun children(): List<LdMark> = list

        fun append(length: Int): LdMark {
            this.length += length
            return this
        }

        fun enter(): LdMark {
            return LdMark(this).apply {
                list += this
            }
        }

        fun exit(): LdMark {
            parent ?: throw IllegalStateException("Can't exit")
            parent.append(length)
            parent.append(length.varintSize)
            return parent
        }
    }
}
