package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.protobuf.json.JsonReader
import com.bybutter.sisyphus.protobuf.json.JsonToken
import com.fasterxml.jackson.core.JsonParser

class JacksonReader(private val parser: JsonParser) : JsonReader {
    override fun peek(): JsonToken {
        return parser.currentToken.token()
    }

    override fun next(): JsonToken {
        return parser.nextToken().token()
    }

    override fun name(): String {
        return parser.currentName
    }

    override fun string(): String {
        return parser.text
    }

    override fun int(): Int {
        return parser.intValue
    }

    override fun long(): Long {
        return parser.longValue
    }

    override fun float(): Float {
        return parser.floatValue
    }

    override fun double(): Double {
        return parser.doubleValue
    }

    override fun bool(): Boolean {
        return parser.booleanValue
    }

    override fun skip() {
        parser.skipChildren()
    }

    companion object {
        private fun com.fasterxml.jackson.core.JsonToken.token(): JsonToken {
            return when (this) {
                com.fasterxml.jackson.core.JsonToken.START_OBJECT -> JsonToken.BEGIN_OBJECT
                com.fasterxml.jackson.core.JsonToken.END_OBJECT -> JsonToken.END_OBJECT
                com.fasterxml.jackson.core.JsonToken.START_ARRAY -> JsonToken.BEGIN_ARRAY
                com.fasterxml.jackson.core.JsonToken.END_ARRAY -> JsonToken.END_ARRAY
                com.fasterxml.jackson.core.JsonToken.FIELD_NAME -> JsonToken.NAME
                com.fasterxml.jackson.core.JsonToken.VALUE_STRING -> JsonToken.STRING
                com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT -> JsonToken.NUMBER
                com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT -> JsonToken.NUMBER
                com.fasterxml.jackson.core.JsonToken.VALUE_TRUE -> JsonToken.BOOL
                com.fasterxml.jackson.core.JsonToken.VALUE_FALSE -> JsonToken.BOOL
                com.fasterxml.jackson.core.JsonToken.VALUE_NULL -> JsonToken.NULL
                else -> throw IllegalStateException()
            }
        }
    }
}
