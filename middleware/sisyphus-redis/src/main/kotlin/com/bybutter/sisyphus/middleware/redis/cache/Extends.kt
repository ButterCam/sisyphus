package com.bybutter.sisyphus.middleware.redis.cache

import java.util.zip.CRC32

fun String.toCRC32(): Long {
    val bytes = this.toByteArray()
    val checksum = CRC32() // java.util.zip.CRC32
    checksum.update(bytes, 0, bytes.size)
    return checksum.value
}
