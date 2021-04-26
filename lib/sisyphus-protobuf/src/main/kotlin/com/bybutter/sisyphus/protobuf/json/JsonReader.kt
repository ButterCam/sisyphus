package com.bybutter.sisyphus.protobuf.json

import com.bybutter.sisyphus.security.base64Decode


interface JsonReader {
    fun peek(): JsonToken

    fun next(): JsonToken

    fun typeToken(): String {
        if (name() != "@type") {
            throw IllegalStateException()
        }
        next()
        return string()
    }

    fun name(): String

    fun string(): String

    fun bytes(): ByteArray {
        return string().base64Decode()
    }

    fun int(): Int

    fun long(): Long

    fun uint(): UInt {
        return int().toUInt()
    }

    fun ulong(): ULong {
        return long().toULong()
    }

    fun float(): Float

    fun double(): Double

    fun bool(): Boolean

    fun nil() {
        if (peek() != JsonToken.NULL) throw IllegalStateException()
    }

    fun skip()
}

enum class JsonToken {
    BEGIN_OBJECT,
    END_OBJECT,
    BEGIN_ARRAY,
    END_ARRAY,
    NAME,
    STRING,
    NUMBER,
    BOOL,
    NULL
}
