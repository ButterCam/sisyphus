package com.bybutter.sisyphus.security

import com.bybutter.sisyphus.data.hex
import java.security.MessageDigest

/**
 * Calculate SHA-1 of string, and convert it to hex string.
 */
fun String.sha1(): String {
    return this.sha1Data().hex()
}

/**
 * Calculate SHA-1 of string.
 */
fun String.sha1Data(): ByteArray {
    return MessageDigest.getInstance("SHA-1").digest(this.toByteArray())
}

/**
 * Calculate SHA-1 of data.
 */
fun ByteArray.sha1(): ByteArray {
    return MessageDigest.getInstance("SHA-1").digest(this)
}
