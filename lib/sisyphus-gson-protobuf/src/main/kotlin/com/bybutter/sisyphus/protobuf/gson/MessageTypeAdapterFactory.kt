package com.bybutter.sisyphus.protobuf.gson

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.ProtobufDefinition
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.protobuf.json.readAny
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

object MessageTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(
        p0: Gson,
        p1: TypeToken<T>,
    ): TypeAdapter<T>? {
        return when (val raw = p1.rawType) {
            Message::class.java -> UnboxedAnyMessageTypeAdapter()
            com.bybutter.sisyphus.protobuf.primitives.Any::class.java -> BoxedAnyMessageTypeAdapter()
            else ->
                if (Message::class.java.isAssignableFrom(raw)) {
                    MessageTypeAdapter(raw)
                } else {
                    null
                }
        } as? TypeAdapter<T>
    }
}

class UnboxedAnyMessageTypeAdapter : TypeAdapter<Message<*, *>>() {
    override fun write(
        writer: JsonWriter,
        message: Message<*, *>,
    ) {
        message.writeTo(GsonWriter(writer))
    }

    override fun read(reader: JsonReader): Message<*, *> {
        return GsonReader(reader).readAny() ?: throw IllegalStateException("Resolve any message failed.")
    }
}

class BoxedAnyMessageTypeAdapter : TypeAdapter<Message<*, *>>() {
    override fun write(
        writer: JsonWriter,
        message: Message<*, *>,
    ) {
        message.writeTo(GsonWriter(writer))
    }

    override fun read(reader: JsonReader): Message<*, *> {
        val message = GsonReader(reader).readAny() ?: throw IllegalStateException("Resolve any message failed.")

        return ProtoReflection.findMessageSupport(com.bybutter.sisyphus.protobuf.primitives.Any.name).invoke {
            set(com.bybutter.sisyphus.protobuf.primitives.Any.TYPE_URL_FIELD_NUMBER, message.support().typeUrl())
            set(com.bybutter.sisyphus.protobuf.primitives.Any.VALUE_FIELD_NUMBER, message.toProto())
        }
    }
}

class MessageTypeAdapter(private val clazz: Class<*>) : TypeAdapter<Message<*, *>>() {
    override fun write(
        writer: JsonWriter,
        message: Message<*, *>,
    ) {
        message.writeTo(GsonWriter(writer))
    }

    override fun read(reader: JsonReader): Message<*, *> {
        val fullName = clazz.getAnnotation(ProtobufDefinition::class.java)?.name ?: throw IllegalStateException()
        return ProtoTypes.findMessageSupport(fullName).invoke {
            readFrom(GsonReader(reader))
        }
    }
}
