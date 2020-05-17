package com.bybutter.sisyphus.data

import java.io.InputStream
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class BitInputStream(private val source: InputStream) : InputStream() {
    private var pos = 8
    private var byte: Int = 0

    override fun read(): Int {
        if (pos > 7) {
            byte = source.read()
            if (byte == -1) {
                return -1
            }
            pos = 0
        }

        return if (byte and (1 shl pos++) > 0) {
            1
        } else {
            0
        }
    }

    fun readBits(byteArray: ByteArray, bits: Int): Int {
        if (bits > byteArray.size * 8) {
            throw IllegalArgumentException()
        }

        var read = 0
        for (i in 0 until bits) {
            if (pos > 7) {
                byte = source.read()
                if (byte == -1) {
                    break
                }
                pos = 0
            }

            read++
            if (byte and (1 shl pos++) > 0) {
                byteArray[i / 8] = byteArray[i / 8] or (1 shl (i % 8)).toByte()
            } else {
                byteArray[i / 8] = byteArray[i / 8] and (1 shl (i % 8)).toByte().inv()
            }
        }

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

class BitBuffer(private val data: ByteArray) {
    private var pos = 0

    fun read(): Int {
        if (pos >= data.size * 8) {
            return -1
        }

        return if (data[pos / 8] and (1 shl (pos % 8)).toByte() > 0) {
            1
        } else {
            0
        }
    }

    fun write(value: Int): Int {
        if (pos >= data.size * 8) {
            return 0
        }

        if (value > 0) {
            data[pos / 8] = data[pos / 8] or (1 shl (pos % 8)).toByte()
        } else {
            data[pos / 8] = data[pos / 8] and (1 shl (pos % 8)).toByte().inv()
        }
        pos++
        return 1
    }

    fun readBits(byteArray: ByteArray, bits: Int): Int {
        if (bits > byteArray.size * 8) {
            throw IllegalArgumentException()
        }

        var read = 0
        for (i in 0 until bits) {
            if (pos >= data.size * 8) {
                break
            }

            read++
            if (data[pos / 8] and (1 shl (pos % 8)).toByte() > 0) {
                byteArray[i / 8] = byteArray[i / 8] or (1 shl (i % 8)).toByte()
            } else {
                byteArray[i / 8] = byteArray[i / 8] and (1 shl (i % 8)).toByte().inv()
            }
            pos++
        }

        return read
    }

    fun writeBits(byteArray: ByteArray, bits: Int): Int {
        if (bits > byteArray.size * 8) {
            throw IllegalArgumentException()
        }

        var written = 0
        for (i in 0 until bits) {
            if (pos >= data.size * 8) {
                break
            }

            written++
            if (byteArray[i / 8] and (1 shl (i % 8)).toByte() > 0) {
                data[pos / 8] = data[pos / 8] or (1 shl (pos % 8)).toByte()
            } else {
                data[pos / 8] = data[pos / 8] and (1 shl (pos % 8)).toByte().inv()
            }
            pos++
        }

        return written
    }

    fun seek(offset: Int): Int {
        pos += offset
        if (pos < 0) {
            pos = 0
        } else if (pos > data.size * 8) {
            pos = data.size * 8
        }
        return pos
    }

    fun toByteArray(): ByteArray {
        return data
    }
}
