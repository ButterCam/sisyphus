package com.bybutter.sisyphus.security

import java.util.zip.CRC32

/**
 * Calculate CRC32 of string.
 */
fun String.crc32(): Long {
    val checksum = CRC32()
    checksum.update(this.toByteArray())
    return checksum.value
}

/**
 * Calculate CRC32 of data.
 */
fun ByteArray.crc32(): Long {
    val checksum = CRC32()
    checksum.update(this)
    return checksum.value
}
