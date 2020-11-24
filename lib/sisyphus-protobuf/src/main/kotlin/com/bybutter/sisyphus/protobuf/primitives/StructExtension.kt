package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.Message

operator fun Struct.Companion.invoke(vararg pairs: Pair<String, kotlin.Any?>): Struct {
    return Struct {
        fields += pairs.associate { it.first to wrapValue(it.second) }
    }
}

operator fun Struct.Companion.invoke(data: Map<String, kotlin.Any?>): Struct {
    return Struct {
        fields += data.entries.associate { it.key to wrapValue(it.value) }
    }
}

operator fun Struct.Companion.invoke(data: Collection<Pair<String, kotlin.Any?>>): Struct {
    return Struct {
        fields += data.associate { it.first to wrapValue(it.second) }
    }
}

fun Struct.unwrapToMap(): Map<String, kotlin.Any?> {
    return this.fields.mapValues {
        unwrapValue(it.value)
    }
}

private fun wrapValue(value: kotlin.Any?): Value {
    return when (value) {
        is Number -> {
            Value {
                kind = Value.Kind.NumberValue(value.toDouble())
            }
        }
        is String -> {
            Value {
                kind = Value.Kind.StringValue(value)
            }
        }
        is NullValue -> {
            Value {
                kind = Value.Kind.NullValue(value)
            }
        }
        is Map<*, *> -> {
            Value {
                kind = Value.Kind.StructValue(Struct(value.entries.map { it.key.toString() to it.value }))
            }
        }
        is List<*> -> {
            Value {
                kind = Value.Kind.ListValue(ListValue {
                    values += value.map { wrapValue(it) }
                })
            }
        }
        is Boolean -> {
            Value {
                kind = Value.Kind.BoolValue(value)
            }
        }
        is Struct -> {
            Value {
                kind = Value.Kind.StructValue(value)
            }
        }
        is Value -> {
            value
        }
        is Message<*, *> -> {
            Value {
                kind = Value.Kind.StructValue(value.asStruct())
            }
        }
        null -> {
            Value {
                kind = Value.Kind.NullValue(NullValue.NULL_VALUE)
            }
        }
        else -> throw UnsupportedOperationException("Unsupported struct value.")
    }
}

private fun unwrapValue(value: Value): kotlin.Any? {
    return when {
        value.hasListValue() -> {
            value.listValue?.values?.map {
                unwrapValue(it)
            }
        }
        value.hasStructValue() -> {
            value.structValue?.fields?.mapValues { unwrapValue(it.value) }
        }
        value.hasStringValue() -> value.stringValue
        value.hasNumberValue() -> value.numberValue
        value.hasBoolValue() -> value.boolValue
        else -> null
    }
}

fun Message<*, *>.asStruct(): Struct {
    val result = mutableListOf<Pair<String, kotlin.Any?>>()

    for ((field, value) in this) {
        val item = if (this.has(field.number)) {
            field.name to value
        } else {
            null
        }

        if (item != null) {
            result.add(item)
        }
    }

    return Struct.invoke(result)
}
