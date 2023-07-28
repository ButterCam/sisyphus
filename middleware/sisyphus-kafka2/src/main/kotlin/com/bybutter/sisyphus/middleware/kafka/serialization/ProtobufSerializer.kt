package com.bybutter.sisyphus.middleware.kafka.serialization

import com.bybutter.sisyphus.jackson.toJson
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.protobuf.primitives.toAny
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Serializer

class ProtobufSerializer<T : Message<*, *>> : Serializer<T>, ProtobufContextInitializer() {

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        init(configs, isKey)
    }

    override fun serialize(topic: String, headers: Headers, data: T): ByteArray {
        val support = headers.lastHeader(PROTOBUF_TYPE)?.let {
            ProtoTypes.findMessageSupport(it.value().decodeToString())
        } ?: messageSupport
        val json = headers.lastHeader(PROTOBUF_USE_JSON)?.let {
            it.value().decodeToString().toBoolean()
        } ?: useJson
        headers.add(PROTOBUF_USE_JSON, json.toString().encodeToByteArray())
        headers.add(PROTOBUF_TYPE, data.support().name.encodeToByteArray())
        return doSerialize(topic, data, json, support)
    }

    override fun serialize(topic: String, data: T): ByteArray {
        return doSerialize(topic, data, useJson, messageSupport)
    }

    private fun doSerialize(topic: String, data: T, useJson: Boolean, support: MessageSupport<*, *>?): ByteArray {
        return if (support == null) {
            if (useJson) {
                data.toAny().toJson().encodeToByteArray()
            } else {
                data.toAny().toProto()
            }
        } else {
            if (support != data.support()) {
                throw IllegalArgumentException("Message type mismatch, accepted message is '${support.name}', but actual message is '${data.support().name}'")
            }
            if (useJson) {
                data.toJson().encodeToByteArray()
            } else {
                data.toProto()
            }
        }
    }
}
