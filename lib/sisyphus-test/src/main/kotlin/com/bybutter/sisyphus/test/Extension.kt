package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.security.base64
import com.bybutter.sisyphus.security.base64Decode
import io.grpc.Metadata

fun Metadata.mergeFrom(data: Map<String, String>): Metadata {
    for ((k, v) in data) {
        if (k.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
            put(Metadata.Key.of(k, Metadata.BINARY_BYTE_MARSHALLER), v.base64Decode())
        } else {
            put(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER), v)
        }
    }
    return this
}

fun Metadata.toMap(): Map<String, String> {
    val result = mutableMapOf<String, String>()
    for (k in keys()) {
        if (k.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
            get(Metadata.Key.of(k, Metadata.BINARY_BYTE_MARSHALLER))?.base64()?.let {
                result[k] = it
            }
        } else {
            get(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER))?.let {
                result[k] = it
            }
        }
    }
    return result
}
