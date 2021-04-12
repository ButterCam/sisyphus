package com.bybutter.sisyphus.protobuf.gson

import com.bybutter.sisyphus.protobuf.json.JsonReader
import com.bybutter.sisyphus.protobuf.json.JsonToken

class GsonReader(val reader: com.google.gson.stream.JsonReader) : JsonReader {
    override fun peek(): JsonToken {
        return when (reader.peek()) {
            com.google.gson.stream.JsonToken.BEGIN_ARRAY -> JsonToken.BEGIN_ARRAY
            com.google.gson.stream.JsonToken.END_ARRAY -> JsonToken.END_ARRAY
            com.google.gson.stream.JsonToken.BEGIN_OBJECT -> JsonToken.BEGIN_OBJECT
            com.google.gson.stream.JsonToken.END_OBJECT -> JsonToken.END_OBJECT
            com.google.gson.stream.JsonToken.NAME -> JsonToken.NAME
            com.google.gson.stream.JsonToken.STRING -> JsonToken.STRING
            com.google.gson.stream.JsonToken.NUMBER -> JsonToken.NUMBER
            com.google.gson.stream.JsonToken.BOOLEAN -> JsonToken.BOOL
            com.google.gson.stream.JsonToken.NULL -> JsonToken.NULL
            else -> throw IllegalStateException()
        }
    }

    override fun nextName(): String {
        return reader.nextName()
    }

    override fun nextString(): String {
        return reader.nextString()
    }

    override fun nextInt(): Int {
        return reader.nextInt()
    }

    override fun nextLong(): Long {
        return reader.nextLong()
    }

    override fun nextFloat(): Float {
        return reader.nextDouble().toFloat()
    }

    override fun nextDouble(): Double {
        return reader.nextDouble()
    }

    override fun nextBool(): Boolean {
        return reader.nextBoolean()
    }

    override fun skip() = with(reader) {
        when (peek()) {
            com.google.gson.stream.JsonToken.BEGIN_ARRAY -> beginArray()
            com.google.gson.stream.JsonToken.END_ARRAY -> endArray()
            com.google.gson.stream.JsonToken.BEGIN_OBJECT -> beginObject()
            com.google.gson.stream.JsonToken.END_OBJECT -> endObject()
            com.google.gson.stream.JsonToken.NAME -> nextName()
            com.google.gson.stream.JsonToken.STRING -> nextString()
            com.google.gson.stream.JsonToken.NUMBER -> nextDouble()
            com.google.gson.stream.JsonToken.BOOLEAN -> nextBoolean()
            com.google.gson.stream.JsonToken.NULL -> nextNull()
            else -> throw IllegalStateException()
        }
        Unit
    }

    override fun skipValue() {
        reader.skipValue()
    }
}
