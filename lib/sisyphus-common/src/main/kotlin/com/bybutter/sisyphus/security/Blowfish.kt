package com.bybutter.sisyphus.security

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun ByteArray.blowfishEncrypt(
    key: String,
    from: Int = 0,
    to: Int = this.size,
): ByteArray {
    return blowfishEncrypt(key.toByteArray(), from, to)
}

fun ByteArray.blowfishDecrypt(
    key: String,
    from: Int = 0,
    to: Int = this.size,
): ByteArray {
    return blowfishDecrypt(key.toByteArray(), from, to)
}

fun ByteArray.blowfishEncrypt(
    key: ByteArray,
    from: Int = 0,
    to: Int = this.size,
): ByteArray {
    val secretKey = SecretKeySpec(key, "Blowfish")
    val cipher = Cipher.getInstance("Blowfish")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher.doFinal(this, from, to)
}

fun ByteArray.blowfishDecrypt(
    key: ByteArray,
    from: Int = 0,
    to: Int = this.size,
): ByteArray {
    val secretKey = SecretKeySpec(key, "Blowfish")
    val cipher = Cipher.getInstance("Blowfish")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    return cipher.doFinal(this, from, to)
}
