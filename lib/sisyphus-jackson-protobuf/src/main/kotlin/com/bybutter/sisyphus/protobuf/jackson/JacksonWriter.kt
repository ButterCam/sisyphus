package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.protobuf.json.JsonWriter
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator

class JacksonWriter(private val gen: JsonGenerator) : JsonWriter {
    override fun beginObject() {
        gen.writeStartObject()
    }

    override fun endObject() {
        gen.writeEndObject()
    }

    override fun beginArray() {
        gen.writeStartArray()
    }

    override fun endArray() {
        gen.writeEndArray()
    }

    override fun nullValue() {
        gen.writeNull()
    }

    override fun value(value: Double) {
        gen.writeNumber(value)
    }

    override fun value(value: Float) {
        gen.writeNumber(value)
    }

    override fun value(value: Int) {
        gen.writeNumber(value)
    }

    override fun value(value: UInt) {
        gen.writeNumber(value.toLong())
    }

    override fun value(value: Long) {
        gen.writeNumber(value)
    }

    override fun value(value: ULong) {
        gen.writeNumber(value.toLong())
    }

    override fun value(value: String) {
        gen.writeString(value)
    }

    override fun value(value: Boolean) {
        gen.writeBoolean(value)
    }

    override fun fieldName(field: FieldDescriptorProto) {
        return fieldName(
            when (gen) {
                is YAMLGenerator -> field.name
                else -> field.jsonName
            },
        )
    }

    override fun fieldName(field: String) {
        gen.writeFieldName(field)
    }
}
