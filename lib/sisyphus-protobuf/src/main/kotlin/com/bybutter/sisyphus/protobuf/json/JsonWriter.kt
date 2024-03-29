package com.bybutter.sisyphus.protobuf.json

import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.security.base64WithPadding

interface JsonWriter {
    fun beginObject()

    fun endObject()

    fun beginArray()

    fun endArray()

    fun nullValue()

    fun value(value: Double)

    fun value(value: Float)

    fun value(value: Int)

    fun value(value: UInt)

    fun value(value: Long)

    fun value(value: ULong)

    fun value(value: String)

    fun value(value: ByteArray) {
        value(value.base64WithPadding())
    }

    fun value(value: Boolean)

    fun value(value: ProtoEnum<*>) {
        value(value.proto)
    }

    fun fieldName(field: String)

    fun fieldName(field: FieldDescriptorProto) {
        fieldName(field.jsonName)
    }

    fun typeToken(support: MessageSupport<*, *>) {
        typeToken(support.typeUrl())
    }

    fun typeToken(typeUrl: String) {
        fieldName("@type")
        value(typeUrl)
    }
}
