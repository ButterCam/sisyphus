package com.bybutter.sisyphus.middleware.kafka.serialization

import com.bybutter.sisyphus.jackson.Json
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.protobuf.jackson.JacksonReader
import com.bybutter.sisyphus.protobuf.primitives.Any
import com.bybutter.sisyphus.protobuf.primitives.toMessage
import com.bybutter.sisyphus.reflect.uncheckedCast
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer
import java.nio.charset.Charset

class ProtobufDeserializer<T : Message<*, *>> : Deserializer<T>, ProtobufContextInitializer() {

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        init(configs, isKey)
    }

    override fun deserialize(topic: String, headers: Headers, data: ByteArray): T {
        val localSupport = headers.lastHeader(PROTOBUF_TYPE)?.let {
            ProtoTypes.findMessageSupport(it.value().decodeToString())
        } ?: messageSupport
        val useJson = headers.lastHeader(PROTOBUF_USE_JSON)?.let {
            it.value().decodeToString().toBoolean()
        } ?: isJson(data)

        return doDeserialize(topic, data, useJson, localSupport)
    }

    override fun deserialize(topic: String, data: ByteArray): T {
        return doDeserialize(topic, data, isJson(data), messageSupport)
    }

    private fun isJson(data: ByteArray): Boolean {
        return data.firstOrNull() == '{'.code.toByte() && data.lastOrNull() == '}'.code.toByte()
    }

    private fun doDeserialize(topic: String, data: ByteArray, useJson: Boolean, support: MessageSupport<*, *>?): T {
        return if (support == null) {
            if (useJson) {
                Json.deserialize(data.toString(Charset.defaultCharset()), Message::class.java)
            } else {
                Any.parse(data).toMessage()
            }.uncheckedCast()
        } else {
            if (useJson) {
                val reader = JacksonReader(Json.mapper.createParser(data)).apply { next() }
                support.invoke {
                    readFrom(reader)
                }
            } else {
                support.parse(data)
            }.uncheckedCast()
        }
    }
}
