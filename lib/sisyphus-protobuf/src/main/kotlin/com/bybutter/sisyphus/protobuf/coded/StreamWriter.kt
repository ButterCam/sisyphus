package com.bybutter.sisyphus.protobuf.coded

import com.bybutter.sisyphus.data.encodeZigZag
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.primitives.Any
import java.io.OutputStream

@OptIn(ExperimentalUnsignedTypes::class)
class StreamWriter(private val stream: OutputStream, mark: MeasureWriter.LdMark) : Writer {
    private val marks = mark.children()
    private var index = 0

    override fun tag(filedNumber: Int, wireType: WireType): StreamWriter {
        return tag(WireType.tagOf(filedNumber, wireType))
    }

    override fun tag(value: Int): StreamWriter {
        return int32(value)
    }

    override fun int32(value: Int): StreamWriter {
        var v = value

        do {
            val bits = v and 0x7F
            v = v ushr 7
            stream.write(bits + (if (v != 0) 0x80 else 0))
        } while (v != 0)
        return this
    }

    override fun uint32(value: UInt): StreamWriter {
        return int32(value.toInt())
    }

    override fun sint32(value: Int): StreamWriter {
        return int32(value.encodeZigZag())
    }

    override fun fixed32(value: UInt): StreamWriter {
        return sfixed32(value.toInt())
    }

    override fun sfixed32(value: Int): StreamWriter {
        stream.write(value and 0xFF)
        stream.write((value ushr 8) and 0xFF)
        stream.write((value ushr 16) and 0xFF)
        stream.write((value ushr 24) and 0xFF)
        return this
    }

    override fun int64(value: Long): StreamWriter {
        var v = value

        do {
            val bits = (v and 0x7F).toInt()
            v = v ushr 7
            stream.write(bits + (if (v != 0L) 0x80 else 0))
        } while (v != 0L)
        return this
    }

    override fun uint64(value: ULong): StreamWriter {
        return int64(value.toLong())
    }

    override fun sint64(value: Long): StreamWriter {
        return int64(value.encodeZigZag())
    }

    override fun fixed64(value: ULong): StreamWriter {
        return sfixed64(value.toLong())
    }

    override fun sfixed64(value: Long): StreamWriter {
        stream.write((value and 0xFF).toInt())
        stream.write(((value ushr 8) and 0xFF).toInt())
        stream.write(((value ushr 16) and 0xFF).toInt())
        stream.write(((value ushr 24) and 0xFF).toInt())
        stream.write(((value ushr 32) and 0xFF).toInt())
        stream.write(((value ushr 40) and 0xFF).toInt())
        stream.write(((value ushr 48) and 0xFF).toInt())
        stream.write(((value ushr 56) and 0xFF).toInt())
        return this
    }

    override fun float(value: Float): StreamWriter {
        return sfixed32(value.toRawBits())
    }

    override fun double(value: Double): StreamWriter {
        return sfixed64(value.toRawBits())
    }

    override fun bool(bool: Boolean): StreamWriter {
        return int32(if (bool) 1 else 0)
    }

    override fun bytes(bytes: ByteArray): StreamWriter {
        int32(bytes.size)
        stream.write(bytes)
        return this
    }

    override fun string(string: String): StreamWriter {
        return bytes(string.toByteArray())
    }

    override fun enum(enum: ProtoEnum): StreamWriter {
        return int32(enum.number)
    }

    override fun message(message: Message<*, *>?): StreamWriter {
        message ?: return this
        beginLd()
        message.writeTo(this)
        return endLd()
    }

    override fun any(message: Message<*, *>?): StreamWriter {
        message ?: return this
        if (message is Any) {
            return message(message)
        }
        return beginLd()
            .tag(1, WireType.LENGTH_DELIMITED).string(message.typeUrl())
            .tag(2, WireType.LENGTH_DELIMITED).message(message)
            .endLd()
    }

    override fun beginLd(): StreamWriter {
        int32(marks[index].length())
        index++
        return this
    }

    override fun endLd(): StreamWriter {
        return this
    }
}
