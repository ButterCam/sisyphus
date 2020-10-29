package com.bybutter.sisyphus.protobuf.coded

import com.bybutter.sisyphus.data.encodeZigZag
import com.bybutter.sisyphus.data.varintSize
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.primitives.Any
import java.io.OutputStream
import java.util.Deque
import java.util.LinkedList

@OptIn(ExperimentalUnsignedTypes::class)
class Writer(private val parent: Writer? = null) {
    private var length: Int = 0
    private val ops: Deque<Op<*>> = LinkedList()

    private fun unshift(op: Op<*>): Writer {
        ops.addFirst(op.also {
            length += it.length
        })
        return this
    }

    private fun push(op: Op<*>): Writer {
        ops.offer(op.also {
            length += it.length
        })
        return this
    }

    fun tag(filedNumber: Int, wireType: WireType): Writer {
        return tag(WireType.tagOf(filedNumber, wireType))
    }

    fun tag(value: Int): Writer {
        return int32(value)
    }

    fun int32(value: Int): Writer {
        return push(Varint32(value))
    }

    fun uint32(value: UInt): Writer {
        return int32(value.toInt())
    }

    fun sint32(value: Int): Writer {
        return int32(value.encodeZigZag())
    }

    fun fixed32(value: UInt): Writer {
        return sfixed32(value.toInt())
    }

    fun sfixed32(value: Int): Writer {
        return push(Fixed32(value))
    }

    fun int64(value: Long): Writer {
        return push(Varint64(value))
    }

    fun uint64(value: ULong): Writer {
        return int64(value.toLong())
    }

    fun sint64(value: Long): Writer {
        return int64(value.encodeZigZag())
    }

    fun fixed64(value: ULong): Writer {
        return sfixed64(value.toLong())
    }

    fun sfixed64(value: Long): Writer {
        return push(Fixed64(value))
    }

    fun float(value: Float): Writer {
        return push(Fixed32(value.toRawBits()))
    }

    fun double(value: Double): Writer {
        return push(Fixed64(value.toRawBits()))
    }

    fun bool(bool: Boolean): Writer {
        return int32(if (bool) 1 else 0)
    }

    fun bytes(bytes: ByteArray): Writer {
        return push(Bytes(bytes))
    }

    fun string(string: String): Writer {
        return bytes(string.toByteArray())
    }

    fun message(message: Message<*, *>?): Writer {
        message ?: return this
        val writer = beginLd()
        message.writeTo(writer)
        return writer.endLd()
    }

    fun any(message: Message<*, *>?): Writer {
        message ?: return beginLd().endLd()
        if (message is Any) {
            return message(message)
        }
        return beginLd()
            .tag(1, WireType.LENGTH_DELIMITED).string(message.typeUrl())
            .tag(2, WireType.LENGTH_DELIMITED).message(message)
            .endLd()
    }

    fun ld(): Writer {
        return unshift(Varint32(length))
    }

    fun beginLd(): Writer {
        return Writer(this)
    }

    fun endLd(): Writer {
        parent ?: throw IllegalStateException("")
        return parent.push(Nested(this.ld()))
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

    private class Nested(override val value: Writer) : Op<Writer> {
        override val length: Int = value.length

        override fun write(outputStream: OutputStream) {
            value.writeTo(outputStream)
        }
    }
}
