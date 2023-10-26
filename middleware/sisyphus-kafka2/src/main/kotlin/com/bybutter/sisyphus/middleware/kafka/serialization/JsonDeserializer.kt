package com.bybutter.sisyphus.middleware.kafka.serialization

import com.bybutter.sisyphus.jackson.Json
import com.fasterxml.jackson.databind.JavaType
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer
import java.lang.reflect.Type

class JsonDeserializer<T> : Deserializer<T> {
    private var type: JavaType? = null

    override fun configure(
        configs: MutableMap<String, *>,
        isKey: Boolean,
    ) {
        val typeConfig = if (isKey) LISTENER_KEY_TYPE else LISTENER_VALUE_TYPE
        configs[typeConfig]?.let {
            type =
                when (it) {
                    is String -> Json.mapper.typeFactory.constructFromCanonical(it)
                    is JavaType -> it
                    is Type -> Json.mapper.typeFactory.constructType(it)
                    else -> throw IllegalArgumentException("Json deserializer config '$typeConfig' must be Type or type name string.")
                }
        }
    }

    override fun deserialize(
        topic: String,
        headers: Headers,
        data: ByteArray,
    ): T {
        val localType =
            headers.lastHeader(JSON_TYPE)?.let {
                Json.mapper.typeFactory.constructFromCanonical(it.value().decodeToString())
            } ?: type
        return doDeserialize(
            topic,
            data,
            localType ?: throw IllegalStateException("Json deserializer must be configured with '$JSON_TYPE' config."),
        )
    }

    override fun deserialize(
        topic: String,
        data: ByteArray,
    ): T {
        return doDeserialize(
            topic,
            data,
            type ?: throw IllegalStateException("Json deserializer must be configured with '$JSON_TYPE' config."),
        )
    }

    private fun doDeserialize(
        topic: String,
        data: ByteArray,
        javaType: JavaType,
    ): T {
        return Json.deserialize(data.inputStream(), javaType)
    }

    companion object {
        const val JSON_TYPE = "sisyphus.kafka.json.type"
    }
}
