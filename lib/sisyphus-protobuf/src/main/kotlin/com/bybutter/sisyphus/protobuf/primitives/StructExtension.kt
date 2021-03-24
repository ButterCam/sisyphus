package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableListValue
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableStruct

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

fun MutableStruct.field(field: String, value: Number) {
    this.fields[field] = Value {
        numberValue = value.toDouble()
    }
}

fun MutableStruct.field(field: String, value: String) {
    this.fields[field] = Value {
        stringValue = value
    }
}

fun MutableStruct.field(field: String, value: Message<*, *>) {
    this.fields[field] = Value {
        structValue = value.asStruct()
    }
}

fun MutableStruct.field(field: String, value: List<*>) {
    this.fields[field] = Value {
        listValue = ListValue {
            values += value.map { wrapValue(it) }
        }
    }
}

fun MutableStruct.field(field: String, value: Struct) {
    this.fields[field] = Value {
        structValue = value
    }
}

fun MutableStruct.struct(field: String, block: MutableStruct.() -> Unit) {
    this.fields[field] = Value {
        structValue = Struct {
            block()
        }
    }
}

fun MutableStruct.list(field: String, block: MutableListValue.() -> Unit) {
    this.fields[field] = Value {
        listValue = ListValue {
            block()
        }
    }
}

fun MutableListValue.value(value: Number) {
    this.values += Value {
        numberValue = value.toDouble()
    }
}

fun MutableListValue.value(value: String) {
    this.values += Value {
        stringValue = value
    }
}

fun MutableListValue.value(value: Message<*, *>) {
    this.values += Value {
        structValue = value.asStruct()
    }
}

fun MutableListValue.value(value: List<*>) {
    this.values += Value {
        listValue = ListValue {
            values += value.map { wrapValue(it) }
        }
    }
}

fun MutableListValue.value(value: Struct) {
    this.values += Value {
        structValue = value
    }
}

fun MutableListValue.struct(block: MutableStruct.() -> Unit) {
    this.values += Value {
        structValue = Struct {
            block()
        }
    }
}

fun MutableListValue.list(block: MutableListValue.() -> Unit) {
    this.values += Value {
        listValue = ListValue {
            block()
        }
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
                kind = Value.Kind.ListValue(
                    ListValue {
                        values += value.map { wrapValue(it) }
                    }
                )
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
    if (this is Struct) return this
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
