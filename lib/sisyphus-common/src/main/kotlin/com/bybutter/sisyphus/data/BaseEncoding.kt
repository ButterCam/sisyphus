package com.bybutter.sisyphus.data

import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.log2
import kotlin.math.truncate

open class BaseEncoding(val table: CharArray) {
    val bits: Int
    val base = table.size
    private val reverseMap: Map<Char, Int> = table.mapIndexed { index, char ->
        char to index
    }.associate { it }

    init {
        val bits = log2(base.toDouble())
        if (bits - truncate(bits) != 0.0) {
            throw IllegalArgumentException("Size of char table must be integer power of 2.")
        }
        this.bits = bits.toInt()
    }

    open fun encode(input: ByteArray): String {
        return encode(input.inputStream())
    }

    open fun encode(input: InputStream): String = buildString {
        val stream = BitInputStream(input)
        val data = IntArray(1)

        do {
            data[0] = 0
            val read = stream.readInt(data, bits)
            if (read > 0) append(table[data[0]])
        } while (read == bits)
    }

    open fun decode(input: String): ByteArray {
        val output = ByteArrayOutputStream()
        val stream = BitOutputStream(output)

        for (char in input) {
            stream.writeInt(reverseMap[char]
                ?: throw IllegalArgumentException("Wrong base$bits input '$char'."), bits)
        }
        return output.toByteArray()
    }

    companion object {
        val base64 = BaseEncoding("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray())

        val base64Url = BaseEncoding("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray())

        val base32 = BaseEncoding("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray())

        val base32Hex = BaseEncoding("0123456789ABCDEFGHIJKLMNOPQRSTUV".toCharArray())
    }
}
