package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.jackson.javaType
import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.CustomProtoTypeSupport
import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.Duration
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
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.security.base64UrlSafeDecode
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Double
import kotlin.Enum
import kotlin.Float
import kotlin.IllegalStateException
import kotlin.Int
import kotlin.Long
import kotlin.OptIn
import kotlin.String
import kotlin.UInt
import kotlin.ULong
import kotlin.UnsupportedOperationException
import kotlin.apply
import kotlin.let
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.jvm.javaType
import kotlin.toUInt
import kotlin.toULong

open class ProtoDeserializer<T : Message<*, *>> : StdDeserializer<T> {

    constructor(type: Class<T>) : super(type)

    constructor(type: JavaType) : super(type)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        return readAny(handledType().javaType, p, ctxt) as? T
    }

    protected fun readAny(type: JavaType, p: JsonParser, ctxt: DeserializationContext): kotlin.Any? {
        if (p.currentToken == JsonToken.VALUE_NULL) {
            return when (type.rawClass) {
                NullValue::class.java -> NullValue.NULL_VALUE
                Value.Kind.NullValue::class.java -> Value.Kind.NullValue(NullValue.NULL_VALUE)
                else -> null
            }
        }

        return when (type.rawClass) {
            Int::class.javaObjectType,
            Int::class.java -> p.intValue
            UInt::class.javaObjectType,
            UInt::class.java -> p.longValue.toUInt()
            Long::class.javaObjectType,
            Long::class.java -> p.longValue
            ULong::class.javaObjectType,
            ULong::class.java -> p.bigIntegerValue.toLong().toULong()
            Double::class.javaObjectType,
            Double::class.java -> p.doubleValue
            Float::class.javaObjectType,
            Float::class.java -> p.floatValue
            Int32Value::class.java -> Int32Value {
                value = p.intValue
            }
            Int64Value::class.java -> Int64Value {
                value = p.longValue
            }
            UInt32Value::class.java -> UInt32Value {
                value = p.longValue.toUInt()
            }
            UInt64Value::class.java -> UInt64Value {
                value = p.bigIntegerValue.toLong().toULong()
            }
            Value.Kind.NumberValue::class.java -> Value.Kind.NumberValue(p.doubleValue)
            ByteArray::class.java -> p.text.base64UrlSafeDecode()
            BytesValue::class.java -> BytesValue {
                value = p.text.base64UrlSafeDecode()
            }
            Boolean::class.javaObjectType,
            Boolean::class.java -> p.booleanValue
            BoolValue::class.java -> BoolValue {
                value = p.booleanValue
            }
            Value.Kind.BoolValue::class.java -> Value.Kind.BoolValue(p.booleanValue)
            String::class.java -> p.text
            StringValue::class.java -> StringValue {
                value = p.text
            }
            Value.Kind.StringValue::class.java -> Value.Kind.StringValue(p.text)
            FieldMask::class.java -> readFieldMask(type, p, ctxt)
            Message::class.java,
            com.bybutter.sisyphus.protobuf.primitives.Any::class.java -> readAnyProto(type, p, ctxt)
            Timestamp::class.java -> readTimestamp(type, p, ctxt)
            Duration::class.java -> readDuration(type, p, ctxt)
            ListValue::class.java -> readList(p, ctxt)
            Value::class.java -> readValue(p, ctxt)
            Struct::class.java -> readStruct(p, ctxt)
            else -> {
                when {
                    Enum::class.java.isAssignableFrom(type.rawClass) -> readEnum(type, p, ctxt)
                    List::class.java.isAssignableFrom(type.rawClass) -> readList(type, p, ctxt)
                    Map::class.java.isAssignableFrom(type.rawClass) -> readMap(type, p, ctxt)
                    Message::class.java.isAssignableFrom(type.rawClass) -> readRawProto(type, p, ctxt)
                    CustomProtoType::class.java.isAssignableFrom(type.rawClass) -> readCustom(type, p, ctxt)
                    else -> throw UnsupportedOperationException("Type '${type.toCanonical()}' not supported in proto.")
                }
            }
        }
    }

    private fun readProtoFields(value: MutableMessage<*, *>, p: JsonParser, ctxt: DeserializationContext) {
        var current = p.currentToken
        while (current != null) {
            if (current == JsonToken.END_OBJECT) {
                break
            }

            if (current != JsonToken.FIELD_NAME) {
                throw IllegalStateException("Read illegal json token '$current', but should be '${JsonToken.FIELD_NAME}'.")
            }

            val propertyName = p.currentName
            val property = value.getProperty(propertyName)
            p.nextToken()
            if (property != null) {
                value[propertyName] = readAny(property.returnType.javaType.javaType, p, ctxt)
            } else {
                p.skipChildren()
            }
            current = p.nextToken()
        }
    }

    @OptIn(InternalProtoApi::class)
    private fun readRawProto(type: JavaType, p: JsonParser, ctxt: DeserializationContext): Message<*, *>? {
        if (p.currentToken != JsonToken.START_OBJECT) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.START_OBJECT}'.")
        }

        val support = type.rawClass.kotlin.companionObjectInstance as MessageSupport<*, *>
        return support.newMutable().apply {
            p.nextToken()
            readProtoFields(this, p, ctxt)
        }
    }

    private fun readCustom(type: JavaType, p: JsonParser, ctxt: DeserializationContext): CustomProtoType<*> {
        val support =
            type.rawClass.kotlin.companionObjectInstance as CustomProtoTypeSupport<CustomProtoType<Any?>, Any?>
        val raw = readAny(support.rawType.javaType, p, ctxt)
        return support(raw)
    }

    @OptIn(InternalProtoApi::class)
    private fun readAnyProto(type: JavaType, p: JsonParser, ctxt: DeserializationContext): Message<*, *>? {
        if (p.currentToken != JsonToken.START_OBJECT) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.START_OBJECT}'.")
        }

        if (p.nextFieldName() != "@type") {
            throw IllegalStateException("Any proto need begin with '@type' field, but be '${p.nextFieldName()}'.")
        }

        val typeUrl = p.nextTextValue()
            ?: throw IllegalStateException("Any proto need '@type' field with String value.")
        val protoSupport = ProtoTypes.findSupport(typeUrl)
            ?: throw IllegalStateException("Proto type '$typeUrl' not registered in current context.")
        val protoType = protoSupport.javaClass.declaringClass

        return when (protoType) {
            Timestamp::class.java,
            Duration::class.java,
            Value::class.java,
            DoubleValue::class.java,
            FloatValue::class.java,
            Int32Value::class.java,
            Int64Value::class.java,
            UInt32Value::class.java,
            UInt64Value::class.java,
            BoolValue::class.java,
            StringValue::class.java,
            BytesValue::class.java,
            ListValue::class.java,
            FieldMask::class.java,
            Struct::class.java -> {
                var result: Any? = null
                var current = p.nextToken()
                while (current != null) {
                    if (current == JsonToken.END_OBJECT) {
                        break
                    }

                    if (current != JsonToken.FIELD_NAME) {
                        throw IllegalStateException("Read illegal json token '$current', but should be '${JsonToken.FIELD_NAME}'.")
                    }

                    val propertyName = p.currentName
                    current = p.nextToken()

                    if (propertyName == "value") {
                        result = readAny(protoType.javaType, p, ctxt)
                    }
                    p.skipChildren()
                    current = p.nextToken()
                }
                return result as Message<*, *>
            }
            else -> {
                val support = protoType.kotlin.companionObjectInstance as MessageSupport<*, *>
                support.newMutable().apply {
                    p.nextToken()
                    readProtoFields(this, p, ctxt)
                }
            }
        }
    }

    private fun readEnum(type: JavaType, p: JsonParser, ctxt: DeserializationContext): Any? {
        val support = type.rawClass.kotlin.companionObjectInstance as EnumSupport<*>
        return when (p.currentToken) {
            JsonToken.VALUE_STRING -> support(p.text)
            JsonToken.VALUE_NUMBER_INT -> support(p.intValue)
            else -> throw IllegalStateException("Enum value muse be number or string in json, but '${p.currentToken}' read.")
        }
    }

    private fun readTimestamp(type: JavaType, p: JsonParser, ctxt: DeserializationContext): Timestamp? {
        if (p.currentToken != JsonToken.VALUE_STRING) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.FIELD_NAME}'.")
        }

        return Timestamp(p.text)
    }

    private fun readDuration(type: JavaType, p: JsonParser, ctxt: DeserializationContext): Duration? {
        if (p.currentToken != JsonToken.VALUE_STRING) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.VALUE_STRING}'.")
        }
        return Duration(p.text)
    }

    private fun readFieldMask(type: JavaType, p: JsonParser, ctxt: DeserializationContext): FieldMask? {
        if (p.currentToken != JsonToken.VALUE_STRING) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.VALUE_STRING}'.")
        }

        return FieldMask {
            paths += p.text.split(',')
        }
    }

    private fun readValue(p: JsonParser, ctxt: DeserializationContext): Value? {
        return Value {
            kind = when (p.currentToken) {
                JsonToken.VALUE_STRING -> {
                    (p.text.toDoubleOrNull()?.let {
                        Value.Kind.NumberValue(it)
                    } ?: Value.Kind.StringValue(p.text))
                }
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.VALUE_NUMBER_FLOAT -> Value.Kind.NumberValue(p.doubleValue)
                JsonToken.VALUE_TRUE,
                JsonToken.VALUE_FALSE -> Value.Kind.BoolValue(p.booleanValue)
                JsonToken.VALUE_NULL -> Value.Kind.NullValue(NullValue.NULL_VALUE)
                JsonToken.START_OBJECT -> Value.Kind.StructValue(readStruct(p, ctxt))
                JsonToken.START_ARRAY -> Value.Kind.ListValue(readList(p, ctxt))
                else -> throw IllegalStateException("Read illegal json token '${p.currentToken}'.")
            }
        }
    }

    private fun readStruct(p: JsonParser, ctxt: DeserializationContext): Struct {
        if (p.currentToken != JsonToken.START_OBJECT) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.START_OBJECT}'.")
        }
        return Struct {
            val result = mutableMapOf<String, Value>()
            var current = p.nextToken()
            while (current != null) {
                if (current == JsonToken.END_OBJECT) {
                    break
                }

                if (current != JsonToken.FIELD_NAME) {
                    throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.FIELD_NAME}'.")
                }

                val propertyName = p.currentName
                current = p.nextToken()
                readValue(p, ctxt)?.let {
                    result[propertyName] = it
                }
                current = p.nextToken()
            }
            fields += result
        }
    }

    private fun readList(p: JsonParser, ctxt: DeserializationContext): ListValue {
        if (p.currentToken != JsonToken.START_ARRAY) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.START_ARRAY}'.")
        }

        return ListValue {
            val result = mutableListOf<Value>()
            var current = p.nextToken()
            while (current != null) {
                if (current == JsonToken.END_ARRAY) {
                    break
                }

                readValue(p, ctxt)?.let {
                    result.add(it)
                }
                current = p.nextToken()
            }
            values += result
        }
    }

    private fun readList(type: JavaType, p: JsonParser, ctxt: DeserializationContext): List<*>? {
        if (p.currentToken != JsonToken.START_ARRAY) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.START_ARRAY}'.")
        }

        val targetType = type.findTypeParameters(List::class.java).first()

        val result = mutableListOf<kotlin.Any?>()
        var current = p.nextToken()
        while (current != null) {
            if (current == JsonToken.END_ARRAY) {
                break
            }

            result += readAny(targetType, p, ctxt)
            current = p.nextToken()
        }
        return result
    }

    private fun readMap(type: JavaType, p: JsonParser, ctxt: DeserializationContext): Map<*, *>? {
        if (p.currentToken != JsonToken.START_OBJECT) {
            throw IllegalStateException("Read illegal json token '${p.currentToken}', but should be '${JsonToken.START_OBJECT}'.")
        }

        val targetType = type.findTypeParameters(Map::class.java)

        val result = mutableMapOf<kotlin.Any, kotlin.Any>()
        var current = p.nextToken()
        while (current != null) {
            if (current == JsonToken.END_OBJECT) {
                break
            }

            if (current != JsonToken.FIELD_NAME) {
                throw IllegalStateException("Read illegal json token '$current', but should be '${JsonToken.FIELD_NAME}'.")
            }

            val propertyName = when (targetType[0].rawClass) {
                Int::class.javaObjectType,
                Int::class.java -> p.currentName.toInt()
                UInt::class.javaObjectType,
                UInt::class.java -> p.currentName.toUInt()
                Long::class.javaObjectType,
                Long::class.java -> p.currentName.toLong()
                ULong::class.javaObjectType,
                ULong::class.java -> p.currentName.toULong()
                Boolean::class.javaObjectType,
                Boolean::class.java -> p.currentName.toBoolean()
                String::class.java -> p.currentName
                else -> throw IllegalStateException("Type of map key must be string or number.")
            }
            current = p.nextToken()
            readAny(targetType[1], p, ctxt)?.let {
                result[propertyName] = it
            }
            current = p.nextToken()
        }
        return result
    }
}
