package com.bybutter.sisyphus.protobuf.json

import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.ProtoTypes
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
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableBoolValue
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableBytesValue
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableDoubleValue
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableDuration
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableFieldMask
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableFloatValue
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableInt32Value
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableInt64Value
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableListValue
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableStringValue
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableStruct
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableTimestamp
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableUInt32Value
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableUInt64Value
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableValue
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.protobuf.primitives.wrapper
import java.time.ZonedDateTime

internal fun MutableMessage<*, *>.readRaw(reader: JsonReader) {
    when (this) {
        is MutableTimestamp -> {
            reader.ensure(JsonToken.STRING)
            val instant = ZonedDateTime.parse(reader.string()).toInstant()
            seconds = instant.epochSecond
            nanos = instant.nano
        }
        is MutableDuration -> {
            reader.ensure(JsonToken.STRING)
            val read = Duration(reader.string())
            seconds = read.seconds
            nanos = read.nanos
        }
        is MutableFieldMask -> {
            reader.ensure(JsonToken.STRING)
            paths += reader.string().split(',').map { it.trim() }
        }
        is MutableStruct -> {
            reader.ensure(JsonToken.BEGIN_OBJECT)
            while (reader.next() != JsonToken.END_OBJECT) {
                fields[reader.nameAndNext()] = reader.readValue()
            }
        }
        is MutableValue -> {
            when (reader.peek()) {
                JsonToken.BEGIN_OBJECT -> structValue = reader.readStruct()
                JsonToken.BEGIN_ARRAY -> listValue = reader.readListValue()
                JsonToken.STRING -> stringValue = reader.string()
                JsonToken.NUMBER -> numberValue = reader.double()
                JsonToken.BOOL -> boolValue = reader.bool()
                JsonToken.NULL -> {
                    reader.nil()
                    nullValue = NullValue.NULL_VALUE
                }
                else -> throw IllegalStateException()
            }
        }
        is MutableListValue -> {
            reader.ensure(JsonToken.BEGIN_ARRAY)
            while (reader.next() != JsonToken.END_ARRAY) {
                values += reader.readValue()
            }
        }
        is MutableDoubleValue -> {
            value = reader.double()
        }
        is MutableFloatValue -> {
            value = reader.float()
        }
        is MutableInt32Value -> {
            value = reader.int()
        }
        is MutableInt64Value -> {
            value = reader.long()
        }
        is MutableUInt32Value -> {
            value = reader.uint()
        }
        is MutableUInt64Value -> {
            value = reader.ulong()
        }
        is MutableBoolValue -> {
            value = reader.bool()
        }
        is MutableStringValue -> {
            value = reader.string()
        }
        is MutableBytesValue -> {
            value = reader.bytes()
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
            reader.skip()
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
        val entry = ProtoTypes.findMapEntryDescriptor(field.typeName)
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
                    FieldDescriptorProto.Type.INT64 -> keyName.toLong()
                    FieldDescriptorProto.Type.FIXED64,
                    FieldDescriptorProto.Type.UINT64 -> keyName.toULong()
                    FieldDescriptorProto.Type.SINT32,
                    FieldDescriptorProto.Type.SFIXED32,
                    FieldDescriptorProto.Type.INT32 -> keyName.toInt()
                    FieldDescriptorProto.Type.UINT32,
                    FieldDescriptorProto.Type.FIXED32 -> keyName.toUInt()
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
        skip()
        return null
    }

    return when (field.type) {
        FieldDescriptorProto.Type.DOUBLE -> double()
        FieldDescriptorProto.Type.FLOAT -> float()
        FieldDescriptorProto.Type.SFIXED64,
        FieldDescriptorProto.Type.SINT64,
        FieldDescriptorProto.Type.INT64 -> long()
        FieldDescriptorProto.Type.FIXED64,
        FieldDescriptorProto.Type.UINT64 -> ulong()
        FieldDescriptorProto.Type.SFIXED32,
        FieldDescriptorProto.Type.SINT32,
        FieldDescriptorProto.Type.INT32 -> int()
        FieldDescriptorProto.Type.UINT32,
        FieldDescriptorProto.Type.FIXED32 -> uint()
        FieldDescriptorProto.Type.BOOL -> bool()
        FieldDescriptorProto.Type.STRING -> string()
        FieldDescriptorProto.Type.GROUP -> TODO()
        FieldDescriptorProto.Type.BYTES -> bytes()
        FieldDescriptorProto.Type.ENUM -> {
            if (field.typeName == NullValue.name) {
                nil()
                NullValue.NULL_VALUE
            } else {
                ProtoTypes.findEnumSupport(field.typeName).invoke(string())
            }
        }
        FieldDescriptorProto.Type.MESSAGE -> {
            when (field.typeName) {
                com.bybutter.sisyphus.protobuf.primitives.Any.name -> readAny()
                Timestamp.name -> Timestamp(string())
                Duration.name -> Duration(string())
                FieldMask.name -> FieldMask(string())
                Struct.name -> readStruct()
                Value.name -> readValue()
                ListValue.name -> readListValue()
                DoubleValue.name -> double().wrapper()
                FloatValue.name -> float().wrapper()
                Int64Value.name -> long().wrapper()
                UInt64Value.name -> ulong().wrapper()
                Int32Value.name -> int().wrapper()
                UInt32Value.name -> uint().wrapper()
                BoolValue.name -> bool().wrapper()
                StringValue.name -> string().wrapper()
                BytesValue.name -> bytes().wrapper()
                else -> {
                    ProtoTypes.findMessageSupport(field.typeName).invoke {
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

internal fun JsonReader.readStruct(): Struct {
    return Struct {
        readRaw(this@readStruct)
    }
}

internal fun JsonReader.readValue(): Value {
    return Value {
        readRaw(this@readValue)
    }
}

internal fun JsonReader.readListValue(): ListValue {
    return ListValue {
        readRaw(this@readListValue)
    }
}

@OptIn(InternalProtoApi::class)
fun JsonReader.readAny(): Message<*, *> {
    ensure(JsonToken.BEGIN_OBJECT)
    next()
    val message: MutableMessage<*, *> = ProtoTypes.findMessageSupport(typeToken()).newMutable()
    when (message) {
        is MutableBoolValue,
        is MutableBytesValue,
        is MutableDoubleValue,
        is MutableDuration,
        is MutableFieldMask,
        is MutableFloatValue,
        is MutableInt32Value,
        is MutableInt64Value,
        is MutableListValue,
        is MutableStringValue,
        is MutableStruct,
        is MutableTimestamp,
        is MutableUInt32Value,
        is MutableUInt64Value,
        is MutableValue -> {
            if (nameAndNext() == "value") {
                message.readRaw(this)
            } else {
                skip()
            }
        }
        else -> message.readFields(this)
    }
    return message
}

internal fun JsonReader.nameAndNext(): String {
    return name().apply {
        next()
    }
}