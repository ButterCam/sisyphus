package com.bybutter.sisyphus.data

import java.io.InputStream
import java.io.OutputStream
import java.util.BitSet

class BitInputStream(private val source: InputStream) : InputStream() {
    private var pos = -1
    private var byte: Int = 0

    override fun read(): Int {
        if (pos < 0) {
            byte = source.read()
            if (byte == -1) {
                return -1
            }
            pos = 7
        }

        return if (byte and (1 shl pos--) > 0) {
            1
        } else {
            0
        }
    }

    fun readBits(data: BitSet, bits: Int): Int {
        if (bits > data.size()) {
            throw IllegalArgumentException()
        }

        var read = 0
        loop@ for (i in 0 until bits) {
            when (read()) {
                0 -> data.set(i, false)
                1 -> data.set(i, true)
                else -> break@loop
            }
            read++
        }
        return read
    }

    fun readInt(value: IntArray, bits: Int): Int {
        if (bits > 32) throw IllegalArgumentException("'bits' must less than or equal to 32.")
        if (value.isEmpty()) throw IllegalArgumentException("'value' must not be empty.")
        var int = 0

        var read = 0
        loop@ for (i in 0 until bits) {
            when (read()) {
                1 -> int = int or (1 shl (bits - i - 1))
                0 -> {
                }
                else -> break@loop
            }
            read++
        }

        value[0] = int
        return read
    }

    override fun available(): Int {
        return super.available() * 8 + (8 - pos)
    }

    override fun close() {
        source.close()
    }

    override fun reset() {
        source.reset()
    }

    override fun skip(n: Long): Long {
        val oldPos = pos
        pos += n.toInt()
        var skipped = 0
        while (pos > 7) {
            pos -= 8
            byte = source.read()
            if (byte == -1) {
                break
            }
            skipped += 8
        }

        return pos.toLong() - oldPos + skipped
    }
}

class BitOutputStream(private val target: OutputStream) : OutputStream() {
    private var pos = 7
    private var byte: Int = 0

    override fun write(b: Int) {
        if (b > 0) {
            byte = byte or (1 shl pos)
        }
        pos--

        if (pos < 0) {
            target.write(byte)
            byte = 0
            pos = 7
        }
    }

    fun writeBits(data: BitSet, bits: Int) {
        if (bits > data.size()) {
            throw IllegalArgumentException()
        }

        for (i in 0 until bits) {
            if (data[i]) {
                write(1)
            } else {
                write(0)
            }
        }
    }

    fun writeInt(value: Int, bits: Int) {
        for (i in 0 until bits) {
            if (value and (1 shl (bits - i - 1)) > 0) {
                write(1)
            } else {
                write(0)
            }
        }
    }

    override fun close() {
        target.close()
    }
}
