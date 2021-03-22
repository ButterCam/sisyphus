package com.bybutter.sisyphus.protobuf.primitives

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun JsonNode.toProto(): Value = Value {
    when {
        this@toProto.isArray -> {
            listValue = ListValue {
                values += this@toProto.map { it.toProto() }
            }
        }
        this@toProto.isObject -> {
            structValue = Struct {
                for ((field, node) in this@toProto.fields()) {
                    fields[field] = node.toProto()
                }
            }
        }
        this@toProto.isNumber -> {
            numberValue = this@toProto.doubleValue()
        }
        this@toProto.isBoolean -> {
            boolValue = this@toProto.booleanValue()
        }
        this@toProto.isTextual -> {
            val text = this@toProto.textValue()
            text.toDoubleOrNull()?.let {
                numberValue = it
            } ?: text.toBooleanOrNull()?.let {
                boolValue = it
            } ?: run {
                stringValue = text
            }
        }
        this@toProto.isNull -> {
            nullValue = NullValue.NULL_VALUE
        }
        else -> throw UnsupportedOperationException("Unknown json type '$this'.")
    }
}

fun ObjectNode.toProto(): Struct = Struct {
    for ((field, node) in this@toProto.fields()) {
        fields[field] = node.toProto()
    }
}

fun String.toBooleanOrNull(): Boolean? {
    return if (this.equals("true", true) || this.equals("false", true)) {
        this.toBoolean()
    } else {
        null
    }
}
