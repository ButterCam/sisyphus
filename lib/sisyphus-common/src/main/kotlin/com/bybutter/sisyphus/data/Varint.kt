package com.bybutter.sisyphus.data

import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

/**
 * Encode int to Varint.
 */
fun Int.toVarint(): ByteArray {
    val sink = ByteArray(varintSize)
    var v = this
    var offset = 0

    do {
        // Encode next 7 bits + terminator bit
        val bits = v and 0x7F
        v = v ushr 7
        val b = (bits + (if (v != 0) 0x80 else 0)).toByte()
        sink[offset++] = b
    } while (v != 0)

    return sink
}

/**
 * Encode long to Varint.
 */
fun Long.toVarint(): ByteArray {
    val sink = ByteArray(varintSize)
    var v = this
    var offset = 0

    do {
        // Encode next 7 bits + terminator bit
        val bits = v and 0x7F
        v = v ushr 7
        val b = (bits + (if (v != 0L) 0x80 else 0)).toByte()
        sink[offset++] = b
    } while (v != 0L)

    return sink
}

/**
 * Decode Varint to int.
 */
fun ByteArray.toVarint32(): Int {
    var offset = 0
    var result = 0
    var shift = 0
    var b: Int
    do {
        if (shift >= 32) {
            // Out of range
            throw IndexOutOfBoundsException("varint too long.")
        }
        // Get 7 bits from next byte
        b = this[offset++].toInt()
        result = result or (b and 0x7F shl shift)
        shift += 7
    } while (b and 0x80 != 0)
    return result
}

/**
 * Decode Varint to long.
 */
fun ByteArray.toVarint64(): Long {
    var offset = 0
    var result = 0L
    var shift = 0
    var b: Long
    do {
        if (shift >= 64) {
            // Out of range
            throw IndexOutOfBoundsException("varint too long.")
        }
        // Get 7 bits from next byte
        b = this[offset++].toLong()
        result = result or (b and 0x7F shl shift)
        shift += 7
    } while (b and 0x80 != 0L)
    return result
}

val Int.varintSize: Int
    get() = max(1, (32 - this.countLeadingZeroBits() + 6) / 7)

val Long.varintSize: Int
    get() = max(1, (64 - this.countLeadingZeroBits() + 6) / 7)

fun Int.toZigZagVarint(): ByteArray {
    return this.encodeZigZag().toVarint()
}

fun Long.toZigZagVarint(): ByteArray {
    return this.encodeZigZag().toVarint()
}

fun ByteArray.toZigZagVarint32(): Int {
    return this.toVarint32().decodeZigZag()
}

fun ByteArray.toZigZagVarint64(): Long {
    return this.toVarint64().decodeZigZag()
}

fun InputStream.readRawVarintData(): ByteArray {
    val output = ByteArrayOutputStream()
    do {
        val t = this.read()
        output.write(t)
    } while (t and 0x80 > 0)
    return output.toByteArray()
}

fun Int.decodeZigZag(): Int {
    return (this ushr 1) xor -(this and 1)
}

fun Long.decodeZigZag(): Long {
    return (this ushr 1) xor -(this and 1)
}

fun Int.encodeZigZag(): Int {
    return (this shl 1) xor (this shr 31)
}

fun Long.encodeZigZag(): Long {
    return (this shl 1) xor (this shr 63)
}
