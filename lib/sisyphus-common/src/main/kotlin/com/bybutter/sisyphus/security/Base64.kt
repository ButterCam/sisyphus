package com.bybutter.sisyphus.security

import java.nio.charset.Charset
import java.util.Base64

/**
 * Encode a string with base64 encoding without padding.
 */
fun String.base64(charset: Charset = Charsets.UTF_8): String {
    return this.toByteArray(charset).base64()
}

/**
 * Encode a string with url safe base64 encoding without padding.
 */
fun String.base64UrlSafe(charset: Charset = Charsets.UTF_8): String {
    return this.toByteArray(charset).base64UrlSafe()
}

/**
 * Encode a string with base64 encoding with padding.
 */
fun String.base64WithPadding(charset: Charset = Charsets.UTF_8): String {
    return this.toByteArray(charset).base64WithPadding()
}

/**
 * Encode a string with url safe base64 encoding with padding.
 */
fun String.base64UrlSafeWithPadding(charset: Charset = Charsets.UTF_8): String {
    return this.toByteArray(charset).base64UrlSafeWithPadding()
}

/**
 * Encode data with base64 encoding without padding.
 */
fun ByteArray.base64(): String {
    return Base64.getEncoder().withoutPadding().encodeToString(this)
}

/**
 * Encode data with url safe base64 encoding without padding.
 */
fun ByteArray.base64UrlSafe(): String {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(this)
}

/**
 * Encode data with base64 encoding with padding.
 */
fun ByteArray.base64WithPadding(): String {
    return Base64.getEncoder().encodeToString(this)
}

/**
 * Encode data with url safe base64 encoding with padding.
 */
fun ByteArray.base64UrlSafeWithPadding(): String {
    return Base64.getUrlEncoder().encodeToString(this).replace('=', '~')
}

/**
 * Decode base64 encoded data.
 */
fun String.base64Decode(): ByteArray {
    return Base64.getDecoder().decode(this)
}

/**
 * Decode url safe base64 encoded data.
 */
fun String.base64UrlSafeDecode(): ByteArray {
    return Base64.getUrlDecoder().decode(this.replace('~', '='))
}
