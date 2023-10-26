package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.protobuf.Message
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

open class ProtoSerializer<T : Message<*, *>> : StdSerializer<T> {
    constructor(type: Class<T>) : super(type)

    constructor(type: JavaType) : super(type)

    override fun serialize(
        value: T,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        value.writeTo(JacksonWriter(gen))
    }
}
