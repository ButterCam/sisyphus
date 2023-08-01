package com.bybutter.sisyphus.middleware.kafka.serialization

import com.bybutter.sisyphus.jackson.toJson
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.primitives.toAny
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Serializer

class ProtobufSerializer<T : Message<*, *>> : Serializer<T> {
    private var useJson: Boolean = false

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        configs[PROTOBUF_USE_JSON]?.let {
            useJson = when (it) {
                is Boolean -> it
                is String -> it.toBoolean()
                else -> throw IllegalArgumentException("Protobuf deserializer config '$PROTOBUF_USE_JSON'($it[${it.javaClass}]) must be boolean or string.")
            }
        }
    }

    override fun serialize(topic: String, headers: Headers, data: T): ByteArray {
        val json = headers.lastHeader(PROTOBUF_USE_JSON)?.let {
            it.value().decodeToString().toBoolean()
        } ?: useJson
        headers.add(PROTOBUF_TYPE, data.support().name.encodeToByteArray())
        return doSerialize(topic, data, json, data.support())
    }

    override fun serialize(topic: String, data: T): ByteArray {
        return doSerialize(topic, data, useJson, null)
    }

    private fun doSerialize(topic: String, data: T, useJson: Boolean, support: MessageSupport<*, *>?): ByteArray {
        return if (support == null) {
            if (useJson) {
                data.toAny().toJson().encodeToByteArray()
            } else {
                data.toAny().toProto()
            }
        } else {
            if (useJson) {
                data.toJson().encodeToByteArray()
            } else {
                data.toProto()
            }
        }
    }
}
