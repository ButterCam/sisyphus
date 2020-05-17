package com.bybutter.sisyphus.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

object Json : JacksonFormatSupport() {
    override val mapper: ObjectMapper by lazy {
        ObjectMapper().findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(JsonParser.Feature.IGNORE_UNDEFINED, true)
                .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
    }
}

inline fun <reified T> String?.parseJsonOrNull(): T? {
    this ?: return null

    return try {
        Json.deserialize(this, object : TypeReference<T>() {})
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T> String?.parseJsonOrDefault(value: T): T {
    return this.parseJsonOrNull<T>() ?: value
}

inline fun <reified T> String.parseJson(): T =
        Json.deserialize(this, object : TypeReference<T>() {})

fun Any.toJson(): String = Json.serialize(this)
