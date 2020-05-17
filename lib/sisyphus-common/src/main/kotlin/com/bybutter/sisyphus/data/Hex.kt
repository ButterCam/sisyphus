package com.bybutter.sisyphus.data

import kotlin.experimental.or

private val hexArray = "0123456789ABCDEF".toCharArray()
private val hexChars = "0123456789ABCDEFabcdef".toSet()

/**
 * Encode data to hex string.
 */
fun ByteArray.hex(): String {
    val hexChars = CharArray(this.size * 2)
    for (index in this.indices) {
        val v = 0xFF and this[index].toInt()
        hexChars[index * 2] = hexArray[v.ushr(4)]
        hexChars[index * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}

/**
 * Decode hex string to data.
 */
fun String.parseHex(): ByteArray {
    if (this.isEmpty()) return byteArrayOf()

    val startIndex = this.length % 2
    val result = ByteArray(this.length / 2 + startIndex)

    for (index in this.indices) {
        val offset = index + startIndex
        if (!hexChars.contains(this[index])) {
            throw IllegalArgumentException("Wrong hex format char '${this[index]}'.")
        }
        val value = when {
            this[index] > 'a' -> 10 + (this[index] - 'a')
            this[index] > 'A' -> 10 + (this[index] - 'A')
            else -> this[index] - '0'
        } shl ((1 - offset % 2) * 4)
        result[offset / 2] = result[offset / 2] or value.toByte()
    }

    return result
}
