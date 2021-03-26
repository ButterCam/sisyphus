package com.bybutter.sisyphus.io

import java.io.OutputStream
import java.util.Objects

class FixedByteArrayOutputStream(
    private val buf: ByteArray,
    private var pos: Int = 0
) : OutputStream() {

    constructor(size: Int) : this(ByteArray(size))

    @Synchronized
    override fun write(b: Int) {
        buf[pos] = b.toByte()
        pos += 1
    }

    @Synchronized
    override fun write(b: ByteArray, off: Int, len: Int) {
        Objects.checkFromIndexSize(off, len, b.size)
        System.arraycopy(b, off, buf, pos, len)
        pos += len
    }

    fun data(): ByteArray {
        return buf
    }
}
