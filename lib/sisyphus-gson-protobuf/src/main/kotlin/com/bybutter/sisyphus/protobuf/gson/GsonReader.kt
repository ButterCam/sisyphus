package com.bybutter.sisyphus.protobuf.gson

import com.bybutter.sisyphus.protobuf.json.JsonReader
import com.bybutter.sisyphus.protobuf.json.JsonToken

class GsonReader(val reader: com.google.gson.stream.JsonReader) : JsonReader {
    private var token: com.google.gson.stream.JsonToken? = null
    private var booleanValue: Boolean? = null
    private var nameValue: String? = null
    private var numberValue: Double? = null
    private var stringValue: String? = null

    private fun bufferedPeek(): com.google.gson.stream.JsonToken {
        if (token == null) {
            token = reader.peek()
            when (token) {
                com.google.gson.stream.JsonToken.NAME -> nameValue = reader.nextName()
                com.google.gson.stream.JsonToken.STRING -> stringValue = reader.nextString()
                com.google.gson.stream.JsonToken.NUMBER -> numberValue = reader.nextDouble()
                com.google.gson.stream.JsonToken.BOOLEAN -> booleanValue = reader.nextBoolean()
                com.google.gson.stream.JsonToken.BEGIN_ARRAY -> reader.beginArray()
                com.google.gson.stream.JsonToken.END_ARRAY -> reader.endArray()
                com.google.gson.stream.JsonToken.BEGIN_OBJECT -> reader.beginObject()
                com.google.gson.stream.JsonToken.END_OBJECT -> reader.endObject()
                com.google.gson.stream.JsonToken.NULL -> reader.nextNull()
            }
        }
        return token!!
    }

    override fun peek(): JsonToken {
        return bufferedPeek().token()
    }

    override fun next(): JsonToken {
        token = null
        return bufferedPeek().token()
    }

    override fun name(): String {
        if (bufferedPeek() == com.google.gson.stream.JsonToken.NAME) return nameValue!! else TODO()
    }

    override fun string(): String {
        if (bufferedPeek() == com.google.gson.stream.JsonToken.STRING) return stringValue!! else TODO()
    }

    override fun int(): Int {
        if (bufferedPeek() == com.google.gson.stream.JsonToken.NUMBER) return numberValue!!.toInt() else TODO()
    }

    override fun long(): Long {
        if (bufferedPeek() == com.google.gson.stream.JsonToken.NUMBER) return numberValue!!.toLong() else TODO()
    }

    override fun float(): Float {
        if (bufferedPeek() == com.google.gson.stream.JsonToken.NUMBER) return numberValue!!.toFloat() else TODO()
    }

    override fun double(): Double {
        if (bufferedPeek() == com.google.gson.stream.JsonToken.NUMBER) return numberValue!! else TODO()
    }

    override fun bool(): Boolean {
        if (bufferedPeek() == com.google.gson.stream.JsonToken.BOOLEAN) return booleanValue!! else TODO()
    }

    override fun skip() {
        reader.skipValue()
        token = null
    }

    companion object {
        private fun com.google.gson.stream.JsonToken.token(): JsonToken {
            return when (this) {
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
    }
}
