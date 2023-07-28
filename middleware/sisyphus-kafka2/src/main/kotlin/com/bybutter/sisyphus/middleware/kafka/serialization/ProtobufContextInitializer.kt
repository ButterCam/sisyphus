package com.bybutter.sisyphus.middleware.kafka.serialization

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.findMessageSupport
import java.lang.reflect.ParameterizedType
import kotlin.reflect.full.companionObjectInstance

abstract class ProtobufContextInitializer {
    protected var useJson: Boolean = false

    protected var messageSupport: MessageSupport<*, *>? = null

    protected fun init(configs: MutableMap<String, *>, isKey: Boolean) {
        configs[PROTOBUF_USE_JSON]?.let {
            useJson = when (it) {
                is Boolean -> it
                is String -> it.toBoolean()
                else -> throw IllegalArgumentException("Protobuf deserializer config '$PROTOBUF_USE_JSON'($it[${it.javaClass}]) must be boolean or string.")
            }
        }
        val typeConfig = if (isKey) LISTENER_KEY_TYPE else LISTENER_VALUE_TYPE
        configs[typeConfig]?.let {
            messageSupport = when (it) {
                is MessageSupport<*, *> -> it
                Message::class.java -> null
                is ParameterizedType -> null
                is Class<*> -> it.kotlin.companionObjectInstance as? MessageSupport<*, *>
                is String -> ProtoTypes.findMessageSupport(it)
                else -> throw IllegalArgumentException("Protobuf deserializer config '$typeConfig'($it[${it.javaClass}]) must be MessageSupport or message type string.")
            }
        }
    }

    companion object {
        const val PROTOBUF_USE_JSON = "sisyphus.kafka.protobuf.json"

        const val PROTOBUF_TYPE = "sisyphus.kafka.protobuf.type"
    }
}
