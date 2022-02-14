package com.bybutter.sisyphus.protobuf.json

import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.findMapEntryDescriptor
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldMask
import com.bybutter.sisyphus.protobuf.primitives.FloatValue
import com.bybutter.sisyphus.protobuf.primitives.Int32Value
import com.bybutter.sisyphus.protobuf.primitives.Int64Value
import com.bybutter.sisyphus.protobuf.primitives.ListValue
import com.bybutter.sisyphus.protobuf.primitives.NullValue
import com.bybutter.sisyphus.protobuf.primitives.StringValue
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.UInt32Value
import com.bybutter.sisyphus.protobuf.primitives.UInt64Value
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.string
import com.bybutter.sisyphus.protobuf.primitives.toMessage

internal fun JsonWriter.fields(value: Message<*, *>) {
    for ((field, fieldValue) in value) {
        if (!value.has(field.number)) {
            continue
        }

        fieldName(field)
        field(fieldValue, field)
    }
}

internal fun JsonWriter.safeLong(value: Long) {
    if (value in Int.MIN_VALUE..Int.MAX_VALUE) {
        value(value)
    } else {
        value(value.toString())
    }
}

internal fun JsonWriter.safeULong(value: ULong) {
    if (value <= UInt.MAX_VALUE) {
        value(value)
    } else {
        value(value.toString())
    }
}

internal fun JsonWriter.field(value: Any?, field: FieldDescriptorProto) {
    if (value == null) return nullValue()
    if (value is CustomProtoType<*>) return field(value.value(), field)
    if (value is Map<*, *>) {
        return map(value, field)
    }
    if (value is List<*>) {
        beginArray()
        value.forEach {
            field(it, field)
        }
        endArray()
        return
    }
    when (field.type) {
        FieldDescriptorProto.Type.DOUBLE -> value(value as Double)
        FieldDescriptorProto.Type.FLOAT -> value(value as Float)
        FieldDescriptorProto.Type.SFIXED64,
        FieldDescriptorProto.Type.SINT64,
        FieldDescriptorProto.Type.INT64 -> safeLong(value as Long)
        FieldDescriptorProto.Type.FIXED64,
        FieldDescriptorProto.Type.UINT64 -> safeULong(value as ULong)
        FieldDescriptorProto.Type.SFIXED32,
        FieldDescriptorProto.Type.SINT32,
        FieldDescriptorProto.Type.INT32 -> value(value as Int)
        FieldDescriptorProto.Type.UINT32,
        FieldDescriptorProto.Type.FIXED32 -> value(value as UInt)
        FieldDescriptorProto.Type.BOOL -> value(value as Boolean)
        FieldDescriptorProto.Type.STRING -> value(value as String)
        FieldDescriptorProto.Type.GROUP -> TODO()
        FieldDescriptorProto.Type.BYTES -> value(value as ByteArray)
        FieldDescriptorProto.Type.ENUM -> {
            if (field.typeName == NullValue.name) {
                nullValue()
            } else {
                value(value as ProtoEnum<*>)
            }
        }
        FieldDescriptorProto.Type.MESSAGE -> {
            when (field.typeName) {
                com.bybutter.sisyphus.protobuf.primitives.Any.name -> {
                    any(value as Message<*, *>)
                }
                Timestamp.name -> {
                    value((value as Timestamp).string())
                }
                Duration.name -> {
                    value((value as Duration).string())
                }
                FieldMask.name -> {
                    value((value as FieldMask).string())
                }
                Struct.name -> {
                    value((value as Struct))
                }
                Value.name -> {
                    value((value as Value))
                }
                DoubleValue.name -> {
                    value((value as DoubleValue).value)
                }
                FloatValue.name -> {
                    value((value as FloatValue).value)
                }
                Int64Value.name -> {
                    value((value as Int64Value).value)
                }
                UInt64Value.name -> {
                    value((value as UInt64Value).value)
                }
                Int32Value.name -> {
                    value((value as Int32Value).value)
                }
                UInt32Value.name -> {
                    value((value as UInt32Value).value)
                }
                BoolValue.name -> {
                    value((value as BoolValue).value)
                }
                StringValue.name -> {
                    value((value as StringValue).value)
                }
                BytesValue.name -> {
                    value((value as BytesValue).value)
                }
                ListValue.name -> {
                    value(value as ListValue)
                }
                else -> {
                    beginObject()
                    fields(value as Message<*, *>)
                    endObject()
                }
            }
        }
    }
}

internal fun JsonWriter.map(value: Map<*, *>, field: FieldDescriptorProto) {
    if (field.typeName.isEmpty()) throw IllegalStateException()
    val entry = ProtoReflection.findMapEntryDescriptor(field.typeName) ?: throw IllegalStateException()
    val valueDescriptor = entry.field.firstOrNull {
        it.number == 2
    } ?: throw IllegalStateException()

    beginObject()
    value.forEach { (k, v) ->
        val key = when (k) {
            is CustomProtoType<*> -> k.value().toString()
            else -> k.toString()
        }
        fieldName(key)
        field(v, valueDescriptor)
    }
    endObject()
}

internal fun JsonWriter.any(value: Message<*, *>) {
    if (value is com.bybutter.sisyphus.protobuf.primitives.Any) {
        return any(value.toMessage())
    }
    beginObject()
    typeToken(value.support())
    when (value) {
        is Timestamp -> {
            fieldName("value")
            value(value.string())
        }
        is Duration -> {
            fieldName("value")
            value(value.string())
        }
        is FieldMask -> {
            fieldName("value")
            value(value.string())
        }
        is Value -> {
            fieldName("value")
            value(value)
        }
        is ListValue -> {
            fieldName("value")
            value(value)
        }
        is Struct -> {
            fieldName("value")
            value(value)
        }
        is DoubleValue -> {
            fieldName("value")
            value(value.value)
        }
        is FloatValue -> {
            fieldName("value")
            value(value.value)
        }
        is Int32Value -> {
            fieldName("value")
            value(value.value)
        }
        is Int64Value -> {
            fieldName("value")
            value(value.value)
        }
        is UInt32Value -> {
            fieldName("value")
            value(value.value)
        }
        is UInt64Value -> {
            fieldName("value")
            value(value.value)
        }
        is BoolValue -> {
            fieldName("value")
            value(value.value)
        }
        is StringValue -> {
            fieldName("value")
            value(value.value)
        }
        is BytesValue -> {
            fieldName("value")
            value(value.value)
        }
        else -> fields(value)
    }
    endObject()
}

internal fun JsonWriter.value(value: Value) {
    when (val kind = value.kind) {
        is Value.Kind.BoolValue -> value(kind.value)
        is Value.Kind.NumberValue -> value(kind.value)
        is Value.Kind.StringValue -> value(kind.value)
        is Value.Kind.StructValue -> value(kind.value)
        is Value.Kind.ListValue -> value(kind.value)
        is Value.Kind.NullValue -> nullValue()
        else -> nullValue()
    }
}

internal fun JsonWriter.value(value: Struct) {
    beginObject()
    value.fields.forEach { (k, v) ->
        fieldName(k)
        value(v)
    }
    endObject()
}

internal fun JsonWriter.value(value: ListValue) {
    beginArray()
    value.values.forEach {
        value(it)
    }
    endArray()
}

internal fun JsonWriter.message(value: Message<*, *>) {
    when (value) {
        is Timestamp -> value(value.string())
        is Duration -> value(value.string())
        is FieldMask -> value(value.string())
        is Value -> value(value)
        is ListValue -> value(value)
        is Struct -> value(value)
        is DoubleValue -> value(value.value)
        is FloatValue -> value(value.value)
        is Int32Value -> value(value.value)
        is Int64Value -> value(value.value)
        is UInt32Value -> value(value.value)
        is UInt64Value -> value(value.value)
        is BoolValue -> value(value.value)
        is StringValue -> value(value.value)
        is BytesValue -> value(value.value)
        is com.bybutter.sisyphus.protobuf.primitives.Any -> any(value)
        else -> {
            beginObject()
            fields(value)
            endObject()
        }
    }
}
