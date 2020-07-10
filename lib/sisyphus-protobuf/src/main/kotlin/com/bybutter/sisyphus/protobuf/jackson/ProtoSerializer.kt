package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.jackson.javaType
import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
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
import com.bybutter.sisyphus.security.base64UrlSafeWithPadding
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import java.lang.reflect.Type
import kotlin.math.round
import kotlin.reflect.jvm.javaType

@OptIn(ExperimentalUnsignedTypes::class)
open class ProtoSerializer<T : Message<*, *>> : StdSerializer<T> {
    companion object {
        private val oneOfCache = mutableMapOf<Type, String>()
    }

    constructor(type: Class<T>) : super(type)

    constructor(type: JavaType) : super(type)

    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        writeAny(value as Any, value.javaType, gen, provider)
    }

    private fun getSerializedPropertyName(field: FieldDescriptorProto, gen: JsonGenerator, provider: SerializerProvider): String {
        return when (gen) {
            is YAMLGenerator -> field.name
            else -> field.jsonName
        }
    }

    protected fun serializeProperties(value: Message<*, *>, gen: JsonGenerator, provider: SerializerProvider) {
        for ((field, fieldValue) in value) {
            if (!value.has(field.number)) {
                continue
            }

            gen.writeFieldName(getSerializedPropertyName(field, gen, provider))
            writeAny(fieldValue!!, TypeFactory.defaultInstance().constructType(value.getProperty(field.number)?.returnType?.javaType), gen, provider)
        }
    }

    protected fun writeAny(value: Any, type: JavaType, gen: JsonGenerator, provider: SerializerProvider) {
        when (value) {
            is NullValue -> writeNull(value, gen, provider)
            is String -> gen.writeString(value)
            is ProtoEnum -> writeEnum(value, gen, provider)
            is Number -> writeNumber(value, gen, provider)
            is ByteArray -> writeBytes(value, gen, provider)
            is Boolean -> writeBoolean(value, gen, provider)
            is Timestamp -> writeTimestamp(value, gen, provider)
            is Duration -> writeDuration(value, gen, provider)
            is Struct -> writeStruct(value, gen, provider)
            is Value -> writeValue(value, gen, provider)
            is DoubleValue -> writeDouble(value, gen, provider)
            is FloatValue -> writeFloat(value, gen, provider)
            is Int64Value -> writeInt64(value, gen, provider)
            is UInt64Value -> writeUInt64(value, gen, provider)
            is Int32Value -> writeInt32(value, gen, provider)
            is UInt32Value -> writeUInt32(value, gen, provider)
            is BoolValue -> writeBoolean(value, gen, provider)
            is StringValue -> writeString(value, gen, provider)
            is BytesValue -> writeBytes(value, gen, provider)
            is ListValue -> writeList(value, gen, provider)
            is FieldMask -> writeFieldMask(value, gen, provider)
            is com.bybutter.sisyphus.protobuf.primitives.Any -> writeAny(value.toMessage(), gen, provider)
            is List<*> -> writeList(value, type, gen, provider)
            is Map<*, *> -> writeMap(value, type, gen, provider)
            is CustomProtoType<*> -> writeCustom(value, gen, provider)
            is Message<*, *> -> {
                if (type.rawClass == Message::class.java) {
                    writeAny(value as Message<*, *>, gen, provider)
                } else {
                    writeRawProto(value, gen, provider)
                }
            }
            else -> {
                throw UnsupportedOperationException("Unsupported type '${value.javaClass}($value)' in proto.")
            }
        }
    }

    protected fun writeRawProto(value: Message<*, *>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        serializeProperties(value, gen, provider)
        gen.writeEndObject()
    }

    protected fun writeCustom(value: CustomProtoType<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val raw = value.raw() ?: NullValue.NULL_VALUE
        writeAny(raw, raw.javaType, gen, provider)
    }

    protected fun writeAnyList(value: List<Message<*, *>>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        for (message in value) {
            writeAny(message, gen, provider)
        }
        gen.writeEndArray()
    }

    protected fun writeAny(value: Message<*, *>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("@type", value.typeUrl())
        when (value) {
            is Timestamp -> {
                gen.writeFieldName("value")
                writeTimestamp(value, gen, provider)
            }
            is Duration -> {
                gen.writeFieldName("value")
                writeDuration(value, gen, provider)
            }
            is Value -> {
                gen.writeFieldName("value")
                writeValue(value, gen, provider)
            }
            is DoubleValue -> {
                gen.writeFieldName("value")
                writeDouble(value, gen, provider)
            }
            is FloatValue -> {
                gen.writeFieldName("value")
                writeFloat(value, gen, provider)
            }
            is Int32Value -> {
                gen.writeFieldName("value")
                writeInt32(value, gen, provider)
            }
            is Int64Value -> {
                gen.writeFieldName("value")
                writeInt64(value, gen, provider)
            }
            is UInt32Value -> {
                gen.writeFieldName("value")
                writeUInt32(value, gen, provider)
            }
            is UInt64Value -> {
                gen.writeFieldName("value")
                writeUInt64(value, gen, provider)
            }
            is BoolValue -> {
                gen.writeFieldName("value")
                writeBoolean(value, gen, provider)
            }
            is StringValue -> {
                gen.writeFieldName("value")
                writeString(value, gen, provider)
            }
            is BytesValue -> {
                gen.writeFieldName("value")
                writeBytes(value, gen, provider)
            }
            is ListValue -> {
                gen.writeFieldName("value")
                writeList(value, gen, provider)
            }
            is FieldMask -> {
                gen.writeFieldName("value")
                writeFieldMask(value, gen, provider)
            }
            is Struct -> {
                gen.writeFieldName("value")
                writeStruct(value, gen, provider)
            }
            else -> serializeProperties(value, gen, provider)
        }
        gen.writeEndObject()
    }

    protected fun writeEnum(value: ProtoEnum, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.proto)
    }

    protected fun writeNumber(value: Number, gen: JsonGenerator, provider: SerializerProvider) {
        if ((value as? Float)?.isInfinite() == true ||
            (value as? Float)?.isNaN() == true ||
            (value as? Double)?.isInfinite() == true ||
            (value as? Double)?.isNaN() == true) {
            gen.writeString(value.toString())
        }

        if (round(value.toDouble()) == value.toDouble()) {
            gen.writeNumber(value.toLong())
        } else {
            gen.writeNumber(value.toString())
        }
    }

    protected fun writeBytes(value: ByteArray, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.base64UrlSafeWithPadding())
    }

    protected fun writeBoolean(value: Boolean, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeBoolean(value)
    }

    protected fun writeTimestamp(value: Timestamp, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.string())
    }

    protected fun writeDuration(value: Duration, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.string())
    }

    protected fun writeStruct(value: Struct, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        for (property in value.fields.keys) {
            val propertyValue = value.fields[property] ?: continue
            gen.writeFieldName(property)
            writeValue(propertyValue, gen, provider)
        }
        gen.writeEndObject()
    }

    protected fun writeValue(value: Value, gen: JsonGenerator, provider: SerializerProvider) {
        when (val kind = value.kind) {
            is Value.Kind.BoolValue -> gen.writeBoolean(kind.value)
            is Value.Kind.NumberValue -> writeNumber(kind.value, gen, provider)
            is Value.Kind.StringValue -> gen.writeString(kind.value)
            is Value.Kind.StructValue -> writeStruct(kind.value, gen, provider)
            is Value.Kind.ListValue -> {
                writeList(kind.value, gen, provider)
            }
            is Value.Kind.NullValue -> gen.writeNull()
            else -> gen.writeNull()
        }
    }

    protected fun writeDouble(value: DoubleValue, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.value)
    }

    protected fun writeFloat(value: FloatValue, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.value)
    }

    protected fun writeInt64(value: Int64Value, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.value)
    }

    protected fun writeUInt64(value: UInt64Value, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.value.toString())
    }

    protected fun writeInt32(value: Int32Value, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.value)
    }

    protected fun writeUInt32(value: UInt32Value, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.value.toString())
    }

    protected fun writeBoolean(value: BoolValue, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeBoolean(value.value)
    }

    protected fun writeString(value: StringValue, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.value)
    }

    protected fun writeBytes(value: BytesValue, gen: JsonGenerator, provider: SerializerProvider) {
        writeBytes(value.value, gen, provider)
    }

    protected fun writeNull(value: NullValue, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNull()
    }

    protected fun writeList(value: ListValue, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        for (item in value.values) {
            writeValue(item, gen, provider)
        }
        gen.writeEndArray()
    }

    protected fun writeList(value: List<*>, type: JavaType, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        for (item in value) {
            item ?: continue
            writeAny(item, type.contentType, gen, provider)
        }
        gen.writeEndArray()
    }

    protected fun writeMap(value: Map<*, *>, type: JavaType, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        for ((key, value) in value) {
            key ?: continue
            value ?: continue
            gen.writeFieldName(key.toString())
            writeAny(value, type.contentType, gen, provider)
        }
        gen.writeEndObject()
    }

    protected fun writeFieldMask(value: FieldMask, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.paths.joinToString(","))
    }
}
