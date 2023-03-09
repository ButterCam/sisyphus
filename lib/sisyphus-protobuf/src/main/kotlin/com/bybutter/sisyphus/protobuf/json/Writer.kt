package com.bybutter.sisyphus.protobuf.json

import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.findMapEntryDescriptor
import com.bybutter.sisyphus.protobuf.primitives.AnyResolveFailed
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
import com.bybutter.sisyphus.protobuf.primitives.durationString
import com.bybutter.sisyphus.protobuf.primitives.fieldMaskString
import com.bybutter.sisyphus.protobuf.primitives.timestampString
import com.bybutter.sisyphus.protobuf.primitives.unwrapAny

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
        FieldDescriptorProto.Type.INT64
        -> safeLong(value as Long)

        FieldDescriptorProto.Type.FIXED64,
        FieldDescriptorProto.Type.UINT64
        -> safeULong(value as ULong)

        FieldDescriptorProto.Type.SFIXED32,
        FieldDescriptorProto.Type.SINT32,
        FieldDescriptorProto.Type.INT32
        -> value(value as Int)

        FieldDescriptorProto.Type.UINT32,
        FieldDescriptorProto.Type.FIXED32
        -> value(value as UInt)

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
            if (!wellknown(field.typeName, value as Message<*, *>)) {
                when (field.typeName) {
                    com.bybutter.sisyphus.protobuf.primitives.Any.name -> {
                        any(value)
                    }

                    else -> {
                        beginObject()
                        fields(value)
                        endObject()
                    }
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

internal fun JsonWriter.wellknown(type: String, value: Message<*, *>): Boolean {
    when (type) {
        Timestamp.name -> value(value.timestampString())

        Duration.name -> value(value.durationString())

        FieldMask.name -> value(value.fieldMaskString())

        DoubleValue.name -> value(value.get<Double>(DoubleValue.VALUE_FIELD_NUMBER))

        FloatValue.name -> value(value.get<Float>(FloatValue.VALUE_FIELD_NUMBER))

        Int64Value.name -> value(value.get<Long>(Int64Value.VALUE_FIELD_NUMBER))

        UInt64Value.name -> value(value.get<ULong>(UInt64Value.VALUE_FIELD_NUMBER))

        Int32Value.name -> value(value.get<Int>(Int32Value.VALUE_FIELD_NUMBER))

        UInt32Value.name -> value(value.get<UInt>(UInt32Value.VALUE_FIELD_NUMBER))

        BoolValue.name -> value(value.get<Boolean>(BoolValue.VALUE_FIELD_NUMBER))

        StringValue.name -> value(value.get<String>(StringValue.VALUE_FIELD_NUMBER))

        BytesValue.name -> value(value.get<ByteArray>(BytesValue.VALUE_FIELD_NUMBER))

        Struct.name -> jsonStruct(value)

        Value.name -> jsonValue(value)

        ListValue.name -> jsonList(value)

        else -> return false
    }

    return true
}

internal fun JsonWriter.any(value: Message<*, *>, recursion: Boolean = true) {
    // If we already unwrapped the value, just encode it to json.
    if (value.type() != com.bybutter.sisyphus.protobuf.primitives.Any.name) {
        return unwrappedAny(value)
    }

    if (!value.annotations().contains(AnyResolveFailed)) {
        if (recursion) {
            // If we got an unresolved Any
            any(value.unwrapAny(), false)
        } else {
            // If we got a nested Any
            unwrappedAny(value)
        }
        return
    }

    // We got a resolve failed Any, encode it with base64 value.
    beginObject()
    typeToken(value.get<String>(com.bybutter.sisyphus.protobuf.primitives.Any.TYPE_URL_FIELD_NUMBER))
    fieldName("value")
    value(value.get<ByteArray>(com.bybutter.sisyphus.protobuf.primitives.Any.VALUE_FIELD_NUMBER))
    endObject()
}

internal fun JsonWriter.unwrappedAny(value: Message<*, *>) {
    beginObject()
    typeToken(value.support())
    when (value.type()) {
        Timestamp.name -> {
            fieldName("value")
            value(value.timestampString())
        }

        Duration.name -> {
            fieldName("value")
            value(value.durationString())
        }

        FieldMask.name -> {
            fieldName("value")
            value(value.fieldMaskString())
        }

        DoubleValue.name -> {
            fieldName("value")
            value(value.get<Double>(DoubleValue.VALUE_FIELD_NUMBER))
        }

        FloatValue.name -> {
            fieldName("value")
            value(value.get<Float>(FloatValue.VALUE_FIELD_NUMBER))
        }

        Int64Value.name -> {
            fieldName("value")
            value(value.get<Long>(Int64Value.VALUE_FIELD_NUMBER))
        }

        UInt64Value.name -> {
            fieldName("value")
            value(value.get<ULong>(UInt64Value.VALUE_FIELD_NUMBER))
        }

        Int32Value.name -> {
            fieldName("value")
            value(value.get<Int>(Int32Value.VALUE_FIELD_NUMBER))
        }

        UInt32Value.name -> {
            fieldName("value")
            value(value.get<UInt>(UInt32Value.VALUE_FIELD_NUMBER))
        }

        BoolValue.name -> {
            fieldName("value")
            value(value.get<Boolean>(BoolValue.VALUE_FIELD_NUMBER))
        }

        StringValue.name -> {
            fieldName("value")
            value(value.get<String>(StringValue.VALUE_FIELD_NUMBER))
        }

        BytesValue.name -> {
            fieldName("value")
            value(value.get<ByteArray>(BytesValue.VALUE_FIELD_NUMBER))
        }

        Struct.name -> {
            fieldName("value")
            jsonStruct(value)
        }

        Value.name -> {
            fieldName("value")
            jsonValue(value)
        }

        ListValue.name -> {
            fieldName("value")
            jsonList(value)
        }

        else -> fields(value)
    }
    endObject()
}

internal fun JsonWriter.jsonValue(value: Message<*, *>) {
    when {
        value.has(Value.BOOL_VALUE_FIELD_NUMBER) -> value(value.get<Boolean>(Value.BOOL_VALUE_FIELD_NUMBER))
        value.has(Value.NUMBER_VALUE_FIELD_NUMBER) -> value(value.get<Double>(Value.NUMBER_VALUE_FIELD_NUMBER))
        value.has(Value.STRING_VALUE_FIELD_NUMBER) -> value(value.get<String>(Value.STRING_VALUE_FIELD_NUMBER))
        value.has(Value.STRUCT_VALUE_FIELD_NUMBER) -> jsonStruct(value[Value.STRUCT_VALUE_FIELD_NUMBER])
        value.has(Value.LIST_VALUE_FIELD_NUMBER) -> jsonList(value[Value.LIST_VALUE_FIELD_NUMBER])
        value.has(Value.NULL_VALUE_FIELD_NAME) -> nullValue()
        else -> nullValue()
    }
}

internal fun JsonWriter.jsonStruct(value: Message<*, *>) {
    beginObject()
    value.get<Map<String, Message<*, *>>>(Struct.FIELDS_FIELD_NUMBER).forEach { (k, v) ->
        fieldName(k)
        jsonValue(v)
    }
    endObject()
}

internal fun JsonWriter.jsonList(value: Message<*, *>) {
    beginArray()
    value.get<List<Message<*, *>>>(ListValue.VALUES_FIELD_NUMBER).forEach {
        jsonValue(it)
    }
    endArray()
}

internal fun JsonWriter.message(value: Message<*, *>) {
    if (!wellknown(value.type(), value)) {
        when (value.type()) {
            com.bybutter.sisyphus.protobuf.primitives.Any.name -> {
                any(value)
            }

            else -> {
                beginObject()
                fields(value)
                endObject()
            }
        }
    }
}
