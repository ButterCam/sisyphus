package com.bybutter.sisyphus.protobuf.gson

import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.json.JsonWriter

class GsonWriter(val writer: com.google.gson.stream.JsonWriter, private val refection: ProtoReflection) : JsonWriter {
    override fun reflection(): ProtoReflection {
        return refection
    }

    override fun beginObject() {
        writer.beginObject()
    }

    override fun endObject() {
        writer.endObject()
    }

    override fun beginArray() {
        writer.beginArray()
    }

    override fun endArray() {
        writer.endArray()
    }

    override fun nullValue() {
        writer.nullValue()
    }

    override fun value(value: Double) {
        writer.value(value)
    }

    override fun value(value: Float) {
        writer.value(value)
    }

    override fun value(value: Int) {
        writer.value(value)
    }

    override fun value(value: UInt) {
        writer.value(value.toLong())
    }

    override fun value(value: Long) {
        writer.value(value)
    }

    override fun value(value: ULong) {
        writer.value(value.toLong())
    }

    override fun value(value: String) {
        writer.value(value)
    }

    override fun value(value: Boolean) {
        writer.value(value)
    }

    override fun fieldName(field: String) {
        writer.name(field)
    }
}
