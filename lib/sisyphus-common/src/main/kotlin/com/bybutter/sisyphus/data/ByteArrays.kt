package com.bybutter.sisyphus.data

import java.nio.ByteBuffer

fun ByteArray.hash(): Int {
    return this.contentHashCode()
}

fun ByteArray.eq(other: ByteArray): Boolean {
    return this.contentEquals(other)
}

fun ByteArray.hashWrapper(): ByteArrayHashingWrapper {
    return ByteArrayHashingWrapper(this)
}

fun Boolean.toByteData(): ByteArray {
    return if (this) byteArrayOf(1) else byteArrayOf(0)
}

fun Byte.toByteData(): ByteArray {
    return byteArrayOf(this)
}

fun Short.toByteData(): ByteArray {
    return ByteBuffer.allocate(2).apply {
        putShort(this@toByteData)
    }.array()
}

fun Int.toByteData(): ByteArray {
    return ByteBuffer.allocate(4).apply {
        putInt(this@toByteData)
    }.array()
}

fun Long.toByteData(): ByteArray {
    return ByteBuffer.allocate(8).apply {
        putLong(this@toByteData)
    }.array()
}

fun Float.toByteData(): ByteArray {
    return ByteBuffer.allocate(4).apply {
        putFloat(this@toByteData)
    }.array()
}

fun Double.toByteData(): ByteArray {
    return ByteBuffer.allocate(8).apply {
        putDouble(this@toByteData)
    }.array()
}

fun ByteArray.toBoolean(): Boolean {
    if (this.size != 1) throw IllegalArgumentException("Data array too big to convert to 'byte'.")
    return this[0].toInt() != 0
}

fun ByteArray.toByte(): Byte {
    if (this.size != 1) throw IllegalArgumentException("Data array too big to convert to 'byte'.")
    return this[0]
}

fun ByteArray.toShort(): Short {
    return ByteBuffer.allocate(2).apply {
        put(this@toShort)
    }.getShort(0)
}

fun ByteArray.toInt(): Int {
    return ByteBuffer.allocate(4).apply {
        put(this@toInt)
    }.getInt(0)
}

fun ByteArray.toLong(): Long {
    return ByteBuffer.allocate(8).apply {
        put(this@toLong)
    }.getLong(0)
}

fun ByteArray.toFloat(): Float {
    return ByteBuffer.allocate(4).apply {
        put(this@toFloat)
    }.getFloat(0)
}

fun ByteArray.toDouble(): Double {
    return ByteBuffer.allocate(8).apply {
        put(this@toDouble)
    }.getDouble(0)
}

fun ByteArray.wrapTo(size: Int): ByteArray {
    if (this.size < size) {
        return ByteArray(size - this.size) + this
    }
    return this
}

class ByteArrayHashingWrapper(val target: ByteArray) {
    override fun hashCode(): Int {
        return target.hash()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ByteArrayHashingWrapper) {
            return false
        }
        return target.eq(other.target)
    }
}
