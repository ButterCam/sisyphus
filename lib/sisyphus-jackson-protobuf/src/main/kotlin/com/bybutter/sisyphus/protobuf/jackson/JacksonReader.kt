package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.protobuf.json.JsonReader
import com.bybutter.sisyphus.protobuf.json.JsonToken
import com.fasterxml.jackson.core.JsonParser

class JacksonReader(private val parser: JsonParser) : JsonReader {
    override fun peek(): JsonToken {
        return when (parser.currentToken) {
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

    override fun nextName(): String {
        return parser.currentName.apply {
            parser.nextToken()
        }
    }

    override fun nextString(): String {
        return parser.text.apply {
            parser.nextToken()
        }
    }

    override fun nextInt(): Int {
        return parser.intValue.apply {
            parser.nextToken()
        }
    }

    override fun nextLong(): Long {
        return parser.longValue.apply {
            parser.nextToken()
        }
    }

    override fun nextFloat(): Float {
        return parser.floatValue.apply {
            parser.nextToken()
        }
    }

    override fun nextDouble(): Double {
        return parser.doubleValue.apply {
            parser.nextToken()
        }
    }

    override fun nextBool(): Boolean {
        return parser.booleanValue.apply {
            parser.nextToken()
        }
    }

    override fun skip() {
        parser.nextToken()
    }

    override fun skipValue() {
        parser.skipChildren()
        parser.nextToken()
    }
}
