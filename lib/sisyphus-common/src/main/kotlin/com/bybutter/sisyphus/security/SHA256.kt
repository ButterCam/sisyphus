package com.bybutter.sisyphus.security

import com.bybutter.sisyphus.data.hex
import java.security.MessageDigest

/**
 * Calculate SHA-256 of string, and convert it to hex string.
 */
fun String.sha256(): String {
    return this.sha1Data().hex()
}

/**
 * Calculate SHA-256 of string.
 */
fun String.sha256Data(): ByteArray {
    return MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
}

/**
 * Calculate SHA-256 of data.
 */
fun ByteArray.sha256(): ByteArray {
    return MessageDigest.getInstance("SHA-256").digest(this)
}
