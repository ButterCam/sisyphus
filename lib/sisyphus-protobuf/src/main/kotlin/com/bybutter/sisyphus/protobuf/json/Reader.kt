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
            val instant = ZonedDateTime.parse(reader.nextString()).toInstant()
            seconds = instant.epochSecond
            nanos = instant.nano
        }
        is MutableDuration -> {
            reader.ensure(JsonToken.STRING)
            val read = Duration(reader.nextString())
            seconds = read.seconds
            nanos = read.nanos
        }
        is MutableFieldMask -> {
            reader.ensure(JsonToken.STRING)
            paths += reader.nextString().split(',').map { it.trim() }
        }
        is MutableStruct -> {
            reader.ensureAndSkip(JsonToken.BEGIN_OBJECT)
            while (reader.peek() != JsonToken.END_OBJECT) {
                fields[reader.nextName()] = reader.readValue()
            }
            reader.skip()
        }
        is MutableValue -> {
            when (reader.peek()) {
                JsonToken.BEGIN_OBJECT -> structValue = reader.readStruct()
                JsonToken.BEGIN_ARRAY -> listValue = reader.readListValue()
                JsonToken.STRING -> stringValue = reader.nextString()
                JsonToken.NUMBER -> numberValue = reader.nextDouble()
                JsonToken.BOOL -> boolValue = reader.nextBool()
                JsonToken.NULL -> {
                    nullValue = NullValue.NULL_VALUE
                    reader.skip()
                }
                else -> throw IllegalStateException()
            }
        }
        is MutableListValue -> {
            reader.ensureAndSkip(JsonToken.BEGIN_ARRAY)
            while (reader.peek() != JsonToken.END_ARRAY) {
                values += reader.readValue()
            }
            reader.skip()
        }
        is MutableDoubleValue -> {
            value = reader.nextDouble()
        }
        is MutableFloatValue -> {
            value = reader.nextFloat()
        }
        is MutableInt32Value -> {
            value = reader.nextInt()
        }
        is MutableInt64Value -> {
            value = reader.nextLong()
        }
        is MutableUInt32Value -> {
            value = reader.nextUInt()
        }
        is MutableUInt64Value -> {
            value = reader.nextULong()
        }
        is MutableBoolValue -> {
            value = reader.nextBool()
        }
        is MutableStringValue -> {
            value = reader.nextString()
        }
        is MutableBytesValue -> {
            value = reader.nextBytes()
        }
        else -> {
            reader.ensureAndSkip(JsonToken.BEGIN_OBJECT)
            readFields(reader)
            reader.skip()
        }
    }
}

internal fun MutableMessage<*, *>.readFields(reader: JsonReader) {
    while (reader.peek() != JsonToken.END_OBJECT) {
        val fieldName = reader.nextName()
        val field = this.support().fieldInfo(fieldName)
        if (field == null) {
            reader.skipValue()
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
            ensureAndSkip(JsonToken.BEGIN_OBJECT)
            val result = mutableMapOf<Any, Any>()
            while (peek() != JsonToken.END_OBJECT) {
                val keyName = nextName()
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
            skip()
            return result
        }
    }

    val result = mutableListOf<Any>()
    ensureAndSkip(JsonToken.BEGIN_ARRAY)
    while (peek() != JsonToken.END_ARRAY) {
        readField(field)?.let { result += it }
    }
    skip()
    return result
}

internal fun JsonReader.readField(field: FieldDescriptorProto): Any? {
    if (peek() == JsonToken.NULL && field.type != FieldDescriptorProto.Type.ENUM) {
        skip()
        return null
    }

    return when (field.type) {
        FieldDescriptorProto.Type.DOUBLE -> nextDouble()
        FieldDescriptorProto.Type.FLOAT -> nextFloat()
        FieldDescriptorProto.Type.SFIXED64,
        FieldDescriptorProto.Type.SINT64,
        FieldDescriptorProto.Type.INT64 -> nextLong()
        FieldDescriptorProto.Type.FIXED64,
        FieldDescriptorProto.Type.UINT64 -> nextULong()
        FieldDescriptorProto.Type.SFIXED32,
        FieldDescriptorProto.Type.SINT32,
        FieldDescriptorProto.Type.INT32 -> nextInt()
        FieldDescriptorProto.Type.UINT32,
        FieldDescriptorProto.Type.FIXED32 -> nextUInt()
        FieldDescriptorProto.Type.BOOL -> nextBool()
        FieldDescriptorProto.Type.STRING -> nextString()
        FieldDescriptorProto.Type.GROUP -> TODO()
        FieldDescriptorProto.Type.BYTES -> nextBytes()
        FieldDescriptorProto.Type.ENUM -> {
            if (field.typeName == NullValue.name) {
                nextNull()
                NullValue.NULL_VALUE
            } else {
                ProtoTypes.findEnumSupport(field.typeName).invoke(nextString())
            }
        }
        FieldDescriptorProto.Type.MESSAGE -> {
            when (field.typeName) {
                com.bybutter.sisyphus.protobuf.primitives.Any.name -> readAny()
                Timestamp.name -> Timestamp(nextString())
                Duration.name -> Duration(nextString())
                FieldMask.name -> FieldMask(nextString())
                Struct.name -> readStruct()
                Value.name -> readValue()
                ListValue.name -> readListValue()
                DoubleValue.name -> nextDouble().wrapper()
                FloatValue.name -> nextFloat().wrapper()
                Int64Value.name -> nextLong().wrapper()
                UInt64Value.name -> nextULong().wrapper()
                Int32Value.name -> nextInt().wrapper()
                UInt32Value.name -> nextUInt().wrapper()
                BoolValue.name -> nextBool().wrapper()
                StringValue.name -> nextString().wrapper()
                BytesValue.name -> nextBytes().wrapper()
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

internal fun JsonReader.ensureAndSkip(token: JsonToken) {
    ensure(token)
    skip()
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
    ensureAndSkip(JsonToken.BEGIN_OBJECT)
    var message: MutableMessage<*, *>? = null
    while (peek() != JsonToken.END_OBJECT) {
        if (message == null) {
            message = ProtoTypes.findMessageSupport(nextTypeToken()).newMutable()
            continue
        }

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
                if (nextName() == "value") {
                    message.readRaw(this)
                } else {
                    skipValue()
                }
            }
            else -> message.readFields(this)
        }
    }
    skip()
    return message ?: throw IllegalStateException()
}
