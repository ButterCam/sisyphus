package com.bybutter.sisyphus.security

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

fun ByteArray.aesEncrypt(key: String, from: Int = 0, to: Int = this.size): ByteArray {
    val keyData = key.toByteArray().sha256()
    val ivData = keyData.md5()
    return this.aesEncrypt(keyData, ivData, from, to)
}

fun ByteArray.aesEncrypt(key: ByteArray, iv: ByteArray, from: Int = 0, to: Int = this.size): ByteArray {
    val ivSpec = IvParameterSpec(iv)
    val keySpec = SecretKeySpec(key, "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
    return cipher.doFinal(this, from, to - from)
}

fun ByteArray.aesDecrypt(key: String, from: Int = 0, to: Int = this.size): ByteArray {
    val keyData = key.toByteArray().sha256()
    val ivData = keyData.md5()
    return this.aesDecrypt(keyData, ivData, from, to)
}

fun ByteArray.aesDecrypt(key: ByteArray, iv: ByteArray, from: Int = 0, to: Int = this.size): ByteArray {
    val ivSpec = IvParameterSpec(iv)
    val keySpec = SecretKeySpec(key, "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
    return cipher.doFinal(this, from, to - from)
}
