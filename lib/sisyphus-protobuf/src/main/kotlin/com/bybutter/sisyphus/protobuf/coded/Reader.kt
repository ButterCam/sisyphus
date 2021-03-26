package com.bybutter.sisyphus.protobuf.coded

import com.bybutter.sisyphus.data.decodeZigZag
import com.bybutter.sisyphus.data.toInt
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.Any
import java.io.EOFException
import java.io.InputStream

@OptIn(ExperimentalUnsignedTypes::class)
class Reader(private val inputStream: InputStream) {
    var readBytes = 0
        private set

    val isAtEnd: Boolean
        get() {
            return when (currentByte) {
                -1 -> true
                -2 -> {
                    currentByte = inputStream.read()
                    currentByte == -1
                }
                else -> false
            }
        }

    private var currentByte: Int = -2

    private fun readByte(): Int {
        if (isAtEnd) throw EOFException("Unexpected end of Protobuf input stream")

        if (currentByte == -2) {
            currentByte = inputStream.read()
        }

        readBytes++
        return currentByte.also {
            currentByte = inputStream.read()
        }
    }

    private fun readNByte(count: Int): ByteArray {
        val result = ByteArray(count)
        if (count == 0) {
            return result
        }
        if (isAtEnd) throw EOFException("Unexpected end of Protobuf input stream")

        var toRead = count
        if (currentByte != -2) {
            result[0] = currentByte.toByte()
            toRead--
        }
        while (toRead > 0) {
            toRead -= inputStream.read(result, count - toRead, toRead)
        }
        readBytes += count
        currentByte = inputStream.read()
        return result
    }

    fun tag(): Int {
        return int32().also {
            WireType.getWireType(it)
            if (WireType.getFieldNumber(it) == 0) {
                throw IllegalStateException("Invalid protobuf message field tag (zero).")
            }
        }
    }

    inline fun nested(block: (Reader) -> Unit) {
        val length = int32()
        val current = readBytes
        while (length > readBytes - current) {
            block(this)
        }
    }

    inline fun packed(wireType: Int, block: (Reader) -> Unit) {
        when (wireType) {
            WireType.VARINT.ordinal -> block(this)
            WireType.LENGTH_DELIMITED.ordinal -> nested(block)
            else -> throw IllegalStateException("Packed value must be VARINT or LENGTH_DELIMITED wire type.")
        }
    }

    inline fun packed(wireType: WireType, block: (Reader) -> Unit) {
        when (wireType) {
            WireType.VARINT -> block(this)
            WireType.LENGTH_DELIMITED -> nested(block)
            else -> throw IllegalStateException("Packed value must be VARINT or LENGTH_DELIMITED wire type.")
        }
    }

    fun int32(): Int {
        var result = 0
        var shift = 0
        do {
            val t = readByte()
            if (t < 0) throw EOFException("Read a 'EOF' in varint decoding.")
            result = result or ((t and 0x7F) shl shift)
            shift += 7
        } while (t and 0x80 > 0)
        return result
    }

    fun sint32(): Int {
        return int32().decodeZigZag()
    }

    fun uint32(): UInt {
        return int32().toUInt()
    }

    fun fixed32(): UInt {
        return sfixed32().toUInt()
    }

    fun sfixed32(): Int {
        var result = 0
        val data = readNByte(4)
        result = result or ((data[0].toInt() and 0xFF) shl 0)
        result = result or ((data[1].toInt() and 0xFF) shl 8)
        result = result or ((data[2].toInt() and 0xFF) shl 16)
        result = result or ((data[3].toInt() and 0xFF) shl 24)
        return result
    }

    fun int64(): Long {
        var result = 0L
        var shift = 0
        do {
            val t = readByte()
            if (t < 0) throw EOFException("Read a 'EOF' in varint decoding.")
            result = result or ((t.toLong() and 0x7F) shl shift)
            shift += 7
        } while (t and 0x80 > 0)
        return result
    }

    fun sint64(): Long {
        return int64().decodeZigZag()
    }

    fun uint64(): ULong {
        return int64().toULong()
    }

    fun fixed64(): ULong {
        return sfixed64().toULong()
    }

    fun sfixed64(): Long {
        var result = 0L
        val data = readNByte(8)
        result = result or ((data[0].toInt() and 0xFF).toLong() shl 0)
        result = result or ((data[1].toInt() and 0xFF).toLong() shl 8)
        result = result or ((data[2].toInt() and 0xFF).toLong() shl 16)
        result = result or ((data[3].toInt() and 0xFF).toLong() shl 24)
        result = result or ((data[4].toInt() and 0xFF).toLong() shl 32)
        result = result or ((data[5].toInt() and 0xFF).toLong() shl 40)
        result = result or ((data[6].toInt() and 0xFF).toLong() shl 48)
        result = result or ((data[7].toInt() and 0xFF).toLong() shl 56)
        return result
    }

    fun float(): Float {
        return Float.fromBits(sfixed32())
    }

    fun double(): Double {
        return Double.fromBits(sfixed64())
    }

    fun bool(): Boolean {
        return int64() != 0L
    }

    fun string(): String {
        val len = int32()
        return readNByte(len).toString(Charsets.UTF_8)
    }

    fun bytes(): ByteArray {
        val len = int32()
        return readNByte(len)
    }

    fun any(): Message<*, *> {
        nested {
            it.tag()
            val typeUrl = it.string()
            it.tag()
            val support = ProtoTypes.findSupport(typeUrl) as? MessageSupport<*, *> ?: return Any {
                this.typeUrl = typeUrl
                this.value = it.bytes()
            }
            return support.parse(it, it.int32())
        }

        throw IllegalStateException("Wrong protobuf state.")
    }

    fun skip(wireType: WireType) {
        when (wireType) {
            WireType.VARINT -> int64()
            WireType.FIXED64 -> fixed64()
            WireType.LENGTH_DELIMITED -> bytes()
            WireType.START_GROUP -> TODO()
            WireType.END_GROUP -> TODO()
            WireType.FIXED32 -> fixed32()
        }
    }

    inline fun <TK, TV> mapEntry(kBlock: (Reader) -> TK, vBlock: (Reader) -> TV, block: (TK, TV) -> Unit) {
        var key: TK? = null
        var value: TV? = null

        nested {
            val tag = tag()
            when (WireType.getFieldNumber(tag)) {
                1 -> key = kBlock(this)
                2 -> value = vBlock(this)
                else -> skip(WireType.getWireType(tag))
            }
        }

        block(
            key ?: throw IllegalStateException("Wrong protobuf map data(key missed)."),
            value ?: throw IllegalStateException("Wrong protobuf map data(value missed).")
        )
    }
}
