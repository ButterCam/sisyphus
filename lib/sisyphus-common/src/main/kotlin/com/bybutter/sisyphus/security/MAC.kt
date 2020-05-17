package com.bybutter.sisyphus.security

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun ByteArray.hmacSha1Encrypt(password: ByteArray): ByteArray {
    val mac = Mac.getInstance("HmacSHA1").apply {
        init(SecretKeySpec(password, "HmacSHA1"))
    }

    return mac.doFinal(this)
}
