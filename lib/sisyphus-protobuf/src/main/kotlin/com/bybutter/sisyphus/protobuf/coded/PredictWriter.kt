package com.bybutter.sisyphus.protobuf.coded

import com.bybutter.sisyphus.data.encodeZigZag
import com.bybutter.sisyphus.data.varintSize
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.primitives.Any
import java.io.OutputStream
import java.util.Deque
import java.util.LinkedList
import java.util.Stack

@OptIn(ExperimentalUnsignedTypes::class)
class PredictWriter : Writer {
    private var length: Int = 0
    private val ops: Deque<Op<*>> = LinkedList()
    private val ldMarks: Stack<LdMark> = Stack()

    private fun unshift(op: Op<*>): PredictWriter {
        ops.addFirst(
            op.also {
                length += it.length
            }
        )
        return this
    }

    private fun push(op: Op<*>): PredictWriter {
        if (ldMarks.empty()) {
            length += op.length
        } else {
            ldMarks.peek().append(op.length)
        }
        ops.offer(op)
        return this
    }

    override fun tag(filedNumber: Int, wireType: WireType): PredictWriter {
        return tag(WireType.tagOf(filedNumber, wireType))
    }

    override fun tag(value: Int): PredictWriter {
        return int32(value)
    }

    override fun int32(value: Int): PredictWriter {
        return push(Varint32(value))
    }

    override fun uint32(value: UInt): PredictWriter {
        return int32(value.toInt())
    }

    override fun sint32(value: Int): PredictWriter {
        return int32(value.encodeZigZag())
    }

    override fun fixed32(value: UInt): PredictWriter {
        return sfixed32(value.toInt())
    }

    override fun sfixed32(value: Int): PredictWriter {
        return push(Fixed32(value))
    }

    override fun int64(value: Long): PredictWriter {
        return push(Varint64(value))
    }

    override fun uint64(value: ULong): PredictWriter {
        return int64(value.toLong())
    }

    override fun sint64(value: Long): PredictWriter {
        return int64(value.encodeZigZag())
    }

    override fun fixed64(value: ULong): PredictWriter {
        return sfixed64(value.toLong())
    }

    override fun sfixed64(value: Long): PredictWriter {
        return push(Fixed64(value))
    }

    override fun float(value: Float): PredictWriter {
        return push(Fixed32(value.toRawBits()))
    }

    override fun double(value: Double): PredictWriter {
        return push(Fixed64(value.toRawBits()))
    }

    override fun bool(bool: Boolean): PredictWriter {
        return int32(if (bool) 1 else 0)
    }

    override fun bytes(bytes: ByteArray): PredictWriter {
        return push(Bytes(bytes))
    }

    override fun string(string: String): PredictWriter {
        return bytes(string.toByteArray())
    }

    override fun enum(enum: ProtoEnum<*>): PredictWriter {
        return int32(enum.number)
    }

    override fun message(message: Message<*, *>?): PredictWriter {
        message ?: return this
        val writer = beginLd()
        message.writeTo(writer)
        return writer.endLd()
    }

    override fun any(message: Message<*, *>?): PredictWriter {
        message ?: return this
        if (message is Any) {
            return message(message)
        }
        return beginLd()
            .tag(1, WireType.LENGTH_DELIMITED).string(message.typeUrl())
            .tag(2, WireType.LENGTH_DELIMITED).message(message)
            .endLd()
    }

    fun ld(): PredictWriter {
        return unshift(Varint32(length))
    }

    override fun beginLd(): PredictWriter {
        val mark = LdMark()
        ldMarks.push(mark)
        ops.offer(mark)
        return this
    }

    override fun endLd(): PredictWriter {
        val mark = ldMarks.pop()
        if (ldMarks.empty()) {
            length += mark.value
            length += mark.length
        } else {
            ldMarks.peek().append(mark.value)
            ldMarks.peek().append(mark.length)
        }
        return this
    }

    fun writeTo(outputStream: OutputStream) {
        for (op in ops) {
            op.write(outputStream)
        }
    }

    private interface Op<T> {
        val value: T

        val length: Int

        fun write(outputStream: OutputStream)
    }

    private class Varint32(override val value: Int) : Op<Int> {
        override val length: Int = value.varintSize

        override fun write(outputStream: OutputStream) {
            var v = value

            do {
                val bits = v and 0x7F
                v = v ushr 7
                outputStream.write(bits + (if (v != 0) 0x80 else 0))
            } while (v != 0)
        }
    }

    private class Fixed32(override val value: Int) : Op<Int> {
        override val length: Int = 4

        override fun write(outputStream: OutputStream) {
            outputStream.write(value and 0xFF)
            outputStream.write((value ushr 8) and 0xFF)
            outputStream.write((value ushr 16) and 0xFF)
            outputStream.write((value ushr 24) and 0xFF)
        }
    }

    private class Varint64(override val value: Long) : Op<Long> {
        override val length: Int = value.varintSize

        override fun write(outputStream: OutputStream) {
            var v = value

            do {
                val bits = (v and 0x7F).toInt()
                v = v ushr 7
                outputStream.write(bits + (if (v != 0L) 0x80 else 0))
            } while (v != 0L)
        }
    }

    private class Fixed64(override val value: Long) : Op<Long> {
        override val length: Int = 8

        override fun write(outputStream: OutputStream) {
            outputStream.write((value and 0xFF).toInt())
            outputStream.write(((value ushr 8) and 0xFF).toInt())
            outputStream.write(((value ushr 16) and 0xFF).toInt())
            outputStream.write(((value ushr 24) and 0xFF).toInt())
            outputStream.write(((value ushr 32) and 0xFF).toInt())
            outputStream.write(((value ushr 40) and 0xFF).toInt())
            outputStream.write(((value ushr 48) and 0xFF).toInt())
            outputStream.write(((value ushr 56) and 0xFF).toInt())
        }
    }

    private class Bytes(override val value: ByteArray) : Op<ByteArray> {
        private val lengthDelimited = Varint32(value.size)

        override val length: Int = lengthDelimited.length + value.size

        override fun write(outputStream: OutputStream) {
            lengthDelimited.write(outputStream)
            outputStream.write(value)
        }
    }

    private class LdMark : Op<Int> {
        override var value: Int = 0
            private set
        override val length: Int get() = value.varintSize

        fun append(length: Int) {
            this.value += length
        }

        override fun write(outputStream: OutputStream) {
            var v = value

            do {
                val bits = v and 0x7F
                v = v ushr 7
                outputStream.write(bits + (if (v != 0) 0x80 else 0))
            } while (v != 0)
        }
    }
}
