package com.bybutter.sisyphus.jackson

import com.bybutter.sisyphus.spi.ServiceLoader
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.io.Reader

object Json : JacksonFormatSupport() {
    override val mapper: ObjectMapper by lazy {
        val mapper = ObjectMapper()
        ServiceLoader.load(JacksonMapperConfigurator::class.java).forEach {
            try {
                it.configure(mapper)
            } catch (_: Exception) {
            }
        }
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
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

inline fun <reified T> String.parseJson(): T = Json.deserialize(this, object : TypeReference<T>() {})

inline fun <reified T> Reader?.parseJsonOrNull(): T? {
    this ?: return null

    return try {
        Json.deserialize(this, object : TypeReference<T>() {})
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T> Reader?.parseJsonOrDefault(value: T): T {
    return this.parseJsonOrNull<T>() ?: value
}

inline fun <reified T> Reader.parseJson(): T = Json.deserialize(this, object : TypeReference<T>() {})

inline fun <reified T> InputStream?.parseJsonOrNull(): T? {
    this ?: return null

    return try {
        Json.deserialize(this, object : TypeReference<T>() {})
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T> InputStream?.parseJsonOrDefault(value: T): T {
    return this.parseJsonOrNull<T>() ?: value
}

inline fun <reified T> InputStream.parseJson(): T = Json.deserialize(this, object : TypeReference<T>() {})

fun Any.toJson(): String = Json.serialize(this)
