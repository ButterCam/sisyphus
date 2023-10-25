package com.bybutter.sisyphus.middleware.kafka.serialization

import com.bybutter.sisyphus.jackson.Json
import org.apache.kafka.common.serialization.Serializer

class JsonSerializer<T> : Serializer<T> {
    override fun serialize(
        topic: String?,
        data: T,
    ): ByteArray {
        return Json.serialize(data as Any).encodeToByteArray()
    }
}
