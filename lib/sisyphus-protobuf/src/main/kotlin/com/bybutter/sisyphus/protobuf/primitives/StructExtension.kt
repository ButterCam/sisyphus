package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableListValue
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableStruct

fun Value.Companion.nullValue(): Value {
    return Value {
        nullValue = NullValue.NULL_VALUE
    }
}

operator fun Value.Companion.invoke(value: Struct): Value {
    return Value {
        structValue = value
    }
}

operator fun Value.Companion.invoke(value: Double): Value {
    return Value {
        numberValue = value
    }
}

operator fun Value.Companion.invoke(value: ListValue): Value {
    return Value {
        listValue = value
    }
}

operator fun Value.Companion.invoke(value: NullValue): Value {
    return Value {
        nullValue = value
    }
}

operator fun Value.Companion.invoke(value: String): Value {
    return Value {
        stringValue = value
    }
}

operator fun Value.Companion.invoke(value: Boolean): Value {
    return Value {
        boolValue = value
    }
}

operator fun Value.Companion.invoke(value: List<*>): Value {
    return Value {
        listValue = ListValue {
            values += value.map { wrapValue(it) }
        }
    }
}

operator fun Value.Companion.invoke(value: Map<*, *>): Value {
    return Value {
        structValue = Struct {
            value.forEach { (k, v) ->
                field(k.toString(), wrapValue(v))
            }
        }
    }
}

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

fun MutableStruct.field(field: String, value: Value) {
    this.fields[field] = value
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

fun MutableListValue.value(value: Value) {
    this.values += value
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
        is Number -> Value(value.toDouble())
        is String -> Value(value)
        is NullValue -> Value.nullValue()
        is Map<*, *> -> Value(Struct(value.entries.map { it.key.toString() to it.value }))
        is List<*> -> Value(value)
        is ListValue -> Value(value)
        is Boolean -> Value(value)
        is Struct -> Value(value)
        is Value -> value
        is Message<*, *> -> Value(value.asStruct())
        null -> Value.nullValue()
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

private fun Message<*, *>.asStruct(): Struct {
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

    return Struct(result)
}
