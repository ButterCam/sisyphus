package com.bybutter.sisyphus.dto.jackson

import com.bybutter.sisyphus.reflect.toType
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.lang.reflect.Type

internal class TypeDeserializer : JsonDeserializer<Type>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Type {
        return p.text.toType()
    }
}

internal class TypeSerializer : JsonSerializer<Type>() {
    override fun serialize(value: Type?, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value?.typeName)
    }
}
