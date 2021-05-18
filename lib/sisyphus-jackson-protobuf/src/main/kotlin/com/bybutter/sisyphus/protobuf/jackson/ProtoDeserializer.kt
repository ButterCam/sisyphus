package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.jackson.javaType
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.ProtobufDefinition
import com.bybutter.sisyphus.protobuf.json.readAny
import com.bybutter.sisyphus.protobuf.primitives.Any
import com.bybutter.sisyphus.protobuf.primitives.toAny
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

open class ProtoDeserializer<T : Message<*, *>> : StdDeserializer<T> {

    constructor(type: Class<T>) : super(type)

    constructor(type: JavaType) : super(type)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        val rawClass = handledType().javaType.rawClass
        return when (rawClass) {
            Any::class.java -> {
                JacksonReader(p).readAny().toAny()
            }
            Message::class.java -> {
                JacksonReader(p).readAny()
            }
            else -> {
                val fullName =
                    rawClass.getAnnotation(ProtobufDefinition::class.java)?.name ?: throw IllegalStateException()
                ProtoTypes.findMessageSupport(fullName).invoke {
                    readFrom(JacksonReader(p))
                }
            }
        } as T
    }
}
