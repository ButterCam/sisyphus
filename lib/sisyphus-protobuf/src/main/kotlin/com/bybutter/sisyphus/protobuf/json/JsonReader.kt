package com.bybutter.sisyphus.protobuf.json

import com.bybutter.sisyphus.security.base64Decode

interface JsonReader {
    fun peek(): JsonToken

    fun nextTypeToken(): String {
        if (nextName() != "@type") {
            throw IllegalStateException()
        }
        return nextString()
    }

    fun nextName(): String

    fun nextString(): String

    fun nextBytes(): ByteArray {
        return nextString().base64Decode()
    }

    fun nextInt(): Int

    fun nextLong(): Long

    fun nextUInt(): UInt {
        return nextInt().toUInt()
    }

    fun nextULong(): ULong {
        return nextLong().toULong()
    }

    fun nextFloat(): Float

    fun nextDouble(): Double

    fun nextBool(): Boolean

    fun nextNull() {
        if (peek() == JsonToken.NULL) {
            skip()
        } else {
            throw IllegalStateException()
        }
    }

    fun skip()

    fun skipValue()
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
