package com.bybutter.sisyphus.protobuf.json

import com.bybutter.sisyphus.security.base64Decode

/**
 * A json token stream reader
 */
interface JsonReader {
    /**
     * Get current json token at pointer.
     */
    fun peek(): JsonToken

    /**
     * Advance and get pointer to next token.
     */
    fun next(): JsonToken

    /**
     * Ensure pointer at [JsonToken.NAME] and get the value, this operation
     * will not advance the pointer.
     */
    fun name(): String

    /**
     * Ensure pointer at [JsonToken.STRING] and get the value, this operation
     * will not advance the pointer.
     */
    fun string(): String

    /**
     * Ensure pointer at [JsonToken.STRING] and get and base64 decode the value,
     * this operation will not advance the pointer.
     */
    fun bytes(): ByteArray {
        return string().base64Decode()
    }

    /**
     * Ensure pointer at [JsonToken.NUMBER] and get the int value, this operation
     * will not advance the pointer.
     */
    fun int(): Int

    /**
     * Ensure pointer at [JsonToken.NUMBER] or [JsonToken.STRING] and get the long value, this operation
     * will not advance the pointer.
     */
    fun long(): Long

    /**
     * Ensure pointer at [JsonToken.NUMBER] and get the uint value, this operation
     * will not advance the pointer.
     */
    fun uint(): UInt {
        return int().toUInt()
    }

    /**
     * Ensure pointer at [JsonToken.NUMBER] or [JsonToken.STRING] and get the ulong value, this operation
     * will not advance the pointer.
     */
    fun ulong(): ULong {
        return long().toULong()
    }

    /**
     * Ensure pointer at [JsonToken.NUMBER] and get the float value, this operation
     * will not advance the pointer.
     */
    fun float(): Float

    /**
     * Ensure pointer at [JsonToken.NUMBER] and get the double value, this operation
     * will not advance the pointer.
     */
    fun double(): Double

    /**
     * Ensure pointer at [JsonToken.BOOL] and get the value, this operation will not
     * advance the pointer.
     */
    fun bool(): Boolean

    /**
     * Ensure pointer at [JsonToken.NULL], this operation will not advance the pointer.
     */
    fun nil() {
        if (peek() != JsonToken.NULL) throw IllegalStateException()
    }

    /**
     * Skip children tokens, if pointer at [JsonToken.BEGIN_ARRAY] or [JsonToken.BEGIN_OBJECT]
     * skip the children tokens inside the array or object, advance pointer to [JsonToken.END_ARRAY]
     * or [JsonToken.END_OBJECT], if pointer at value tokens, do noting.
     */
    fun skipChildren() {
        when (peek()) {
            JsonToken.BEGIN_ARRAY, JsonToken.BEGIN_OBJECT -> {}
            else -> return
        }
        var stack = 1
        while (stack > 0) {
            when (next()) {
                JsonToken.BEGIN_ARRAY, JsonToken.BEGIN_OBJECT -> stack++
                JsonToken.END_ARRAY, JsonToken.END_OBJECT -> stack--
            }
        }
    }
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
