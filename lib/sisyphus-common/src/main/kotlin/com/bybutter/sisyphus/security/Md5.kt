package com.bybutter.sisyphus.security

import com.bybutter.sisyphus.data.hex
import java.security.MessageDigest

/**
 * Calculate md5 of string, and convert it to hex string.
 */
fun String.md5(): String {
    return this.md5Data().hex()
}

/**
 * Calculate md5 of string.
 */
fun String.md5Data(): ByteArray {
    return MessageDigest.getInstance("MD5").digest(this.toByteArray())
}

/**
 * Calculate md5 of data.
 */
fun ByteArray.md5(): ByteArray {
    return MessageDigest.getInstance("MD5").digest(this)
}
