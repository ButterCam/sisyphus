package com.bybutter.sisyphus.protobuf.json

import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.findEnumSupport
import com.bybutter.sisyphus.protobuf.findMapEntryDescriptor
import com.bybutter.sisyphus.protobuf.findMessageSupport
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
import com.bybutter.sisyphus.protobuf.primitives.parsePayload
import com.bybutter.sisyphus.protobuf.primitives.tryParsePayload

internal fun MutableMessage<*, *>.readRaw(reader: JsonReader) {
    when (this.type()) {
        Timestamp.name -> {
            reader.ensure(JsonToken.STRING)
            val (seconds, nanos) = Timestamp.parsePayload(reader.string())
            set(Timestamp.SECONDS_FIELD_NUMBER, seconds)
            set(Timestamp.NANOS_FIELD_NUMBER, nanos)
        }

        Duration.name -> {
            reader.ensure(JsonToken.STRING)
            val (seconds, nanos) = Duration.tryParsePayload(reader.string()) ?: (0L to 0)
            set(Duration.SECONDS_FIELD_NUMBER, seconds)
            set(Duration.NANOS_FIELD_NUMBER, nanos)
        }

        FieldMask.name -> {
            reader.ensure(JsonToken.STRING)
            set(FieldMask.PATHS_FIELD_NUMBER, reader.string().split(',').map { it.trim() })
        }

        Struct.name -> {
            reader.ensure(JsonToken.BEGIN_OBJECT)
            val map = mutableMapOf<Any, Any>()
            while (reader.next() != JsonToken.END_OBJECT) {
                map[reader.nameAndNext()] = reader.readValue()
            }
            set(Struct.FIELDS_FIELD_NUMBER, map)
        }

        Value.name -> {
            when (reader.peek()) {
                JsonToken.BEGIN_OBJECT -> set(Value.STRUCT_VALUE_FIELD_NUMBER, reader.readStruct())
                JsonToken.BEGIN_ARRAY -> set(Value.LIST_VALUE_FIELD_NUMBER, reader.readListValue())
                JsonToken.STRING -> set(Value.STRING_VALUE_FIELD_NUMBER, reader.string())
                JsonToken.NUMBER -> set(Value.NUMBER_VALUE_FIELD_NUMBER, reader.double())
                JsonToken.BOOL -> set(Value.BOOL_VALUE_FIELD_NUMBER, reader.bool())
                JsonToken.NULL -> {
                    reader.nil()
                    set(Value.NULL_VALUE_FIELD_NUMBER, ProtoReflection.findEnumSupport(NullValue.name).invoke(0))
                }

                else -> throw IllegalStateException()
            }
        }

        ListValue.name -> {
            reader.ensure(JsonToken.BEGIN_ARRAY)
            val list = mutableListOf<Message<*, *>>()
            while (reader.next() != JsonToken.END_ARRAY) {
                list += reader.readValue()
            }
            set(ListValue.VALUES_FIELD_NUMBER, list)
        }

        DoubleValue.name -> {
            set(DoubleValue.VALUE_FIELD_NUMBER, reader.double())
        }

        FloatValue.name -> {
            set(FloatValue.VALUE_FIELD_NUMBER, reader.float())
        }

        Int32Value.name -> {
            set(Int32Value.VALUE_FIELD_NUMBER, reader.int())
        }

        Int64Value.name -> {
            set(Int64Value.VALUE_FIELD_NUMBER, reader.long())
        }

        UInt32Value.name -> {
            set(UInt32Value.VALUE_FIELD_NUMBER, reader.int())
        }

        UInt64Value.name -> {
            set(UInt64Value.VALUE_FIELD_NUMBER, reader.long())
        }

        BoolValue.name -> {
            set(BoolValue.VALUE_FIELD_NUMBER, reader.bool())
        }

        StringValue.name -> {
            set(StringValue.VALUE_FIELD_NUMBER, reader.string())
        }

        BytesValue.name -> {
            set(BytesValue.VALUE_FIELD_NUMBER, reader.bytes())
        }

        else -> {
            reader.ensure(JsonToken.BEGIN_OBJECT)
            readFields(reader)
        }
    }
}

internal fun MutableMessage<*, *>.readFields(reader: JsonReader) {
    while (reader.next() != JsonToken.END_OBJECT) {
        val fieldName = reader.nameAndNext()
        val field = this.support().fieldInfo(fieldName)
        if (field == null) {
            reader.skipChildren()
            continue
        }
        val value = if (field.label == FieldDescriptorProto.Label.REPEATED) {
            reader.readRepeated(field)
        } else {
            reader.readField(field) ?: continue
        }
        set(field.number, value)
    }
}

internal fun JsonReader.readRepeated(field: FieldDescriptorProto): Any {
    if (field.type == FieldDescriptorProto.Type.MESSAGE) {
        val entry = ProtoReflection.findMapEntryDescriptor(field.typeName)
        if (entry != null) {
            val keyField = entry.field.first { it.number == 1 }
            val valueField = entry.field.first { it.number == 2 }
            ensure(JsonToken.BEGIN_OBJECT)
            val result = mutableMapOf<Any, Any>()
            while (next() != JsonToken.END_OBJECT) {
                val keyName = nameAndNext()
                val value = readField(valueField) ?: continue

                val key = when (keyField.type) {
                    FieldDescriptorProto.Type.SINT64,
                    FieldDescriptorProto.Type.SFIXED64,
                    FieldDescriptorProto.Type.INT64,
                    -> keyName.toLong()

                    FieldDescriptorProto.Type.FIXED64,
                    FieldDescriptorProto.Type.UINT64,
                    -> keyName.toULong()

                    FieldDescriptorProto.Type.SINT32,
                    FieldDescriptorProto.Type.SFIXED32,
                    FieldDescriptorProto.Type.INT32,
                    -> keyName.toInt()

                    FieldDescriptorProto.Type.UINT32,
                    FieldDescriptorProto.Type.FIXED32,
                    -> keyName.toUInt()

                    FieldDescriptorProto.Type.BOOL -> keyName == "true"
                    FieldDescriptorProto.Type.STRING -> keyName
                    else -> throw IllegalStateException()
                }
                result[key] = value
            }
            return result
        }
    }

    val result = mutableListOf<Any>()
    ensure(JsonToken.BEGIN_ARRAY)
    while (next() != JsonToken.END_ARRAY) {
        readField(field)?.let { result += it }
    }
    return result
}

internal fun JsonReader.readField(field: FieldDescriptorProto): Any? {
    if (peek() == JsonToken.NULL && field.type != FieldDescriptorProto.Type.ENUM) {
        skipChildren()
        return null
    }

    return when (field.type) {
        FieldDescriptorProto.Type.DOUBLE -> double()
        FieldDescriptorProto.Type.FLOAT -> float()
        FieldDescriptorProto.Type.SFIXED64,
        FieldDescriptorProto.Type.SINT64,
        FieldDescriptorProto.Type.INT64,
        -> long()

        FieldDescriptorProto.Type.FIXED64,
        FieldDescriptorProto.Type.UINT64,
        -> ulong()

        FieldDescriptorProto.Type.SFIXED32,
        FieldDescriptorProto.Type.SINT32,
        FieldDescriptorProto.Type.INT32,
        -> int()

        FieldDescriptorProto.Type.UINT32,
        FieldDescriptorProto.Type.FIXED32,
        -> uint()

        FieldDescriptorProto.Type.BOOL -> bool()
        FieldDescriptorProto.Type.STRING -> string()
        FieldDescriptorProto.Type.GROUP -> TODO()
        FieldDescriptorProto.Type.BYTES -> bytes()
        FieldDescriptorProto.Type.ENUM -> {
            if (field.typeName == NullValue.name) {
                nil()
                NullValue.NULL_VALUE
            } else {
                when (peek()) {
                    JsonToken.STRING -> ProtoReflection.findEnumSupport(field.typeName).invoke(string())
                    JsonToken.NUMBER -> ProtoReflection.findEnumSupport(field.typeName).invoke(int())
                    else -> TODO()
                }
            }
        }

        FieldDescriptorProto.Type.MESSAGE -> {
            when (field.typeName) {
                com.bybutter.sisyphus.protobuf.primitives.Any.name -> readAny()
                else -> {
                    ProtoReflection.findMessageSupport(field.typeName).invoke {
                        readRaw(this@readField)
                    }
                }
            }
        }
    }
}

internal fun JsonReader.ensure(token: JsonToken) {
    if (peek() != token) throw IllegalStateException()
}

internal fun JsonReader.readStruct(): Message<*, *> {
    return ProtoReflection.findMessageSupport(Struct.name).invoke {
        readRaw(this@readStruct)
    }
}

internal fun JsonReader.readValue(): Message<*, *> {
    return ProtoReflection.findMessageSupport(Value.name).invoke {
        readRaw(this@readValue)
    }
}

internal fun JsonReader.readListValue(): Message<*, *> {
    return ProtoReflection.findMessageSupport(ListValue.name).invoke {
        readRaw(this@readListValue)
    }
}

@OptIn(InternalProtoApi::class)
fun JsonReader.readAny(): Message<*, *>? {
    ensure(JsonToken.BEGIN_OBJECT)
    next()
    val typeUrl = typeToken()
    val support = ProtoReflection.findSupport(typeUrl) as? MessageSupport<*, *> ?: return null
    val message: MutableMessage<*, *> = support.newMutable()
    when (message.type()) {
        BoolValue.name,
        BytesValue.name,
        DoubleValue.name,
        Duration.name,
        FieldMask.name,
        FloatValue.name,
        Int32Value.name,
        Int64Value.name,
        ListValue.name,
        StringValue.name,
        Struct.name,
        Timestamp.name,
        UInt32Value.name,
        UInt64Value.name,
        Value.name,
        -> {
            while (next() != JsonToken.END_OBJECT) {
                if (nameAndNext() == "value") {
                    message.readRaw(this)
                } else {
                    skipChildren()
                }
            }
        }

        else -> message.readFields(this)
    }
    return message
}

/**
 * Ensure pointer at [JsonToken.NAME] and the field name is '@type',
 * ensure the field value is string, read the string value and advance
 * pointer to the string value.
 */
fun JsonReader.typeToken(): String {
    if (name() != "@type") {
        throw IllegalStateException()
    }
    next()
    return string()
}

internal fun JsonReader.nameAndNext(): String {
    return name().apply {
        next()
    }
}
