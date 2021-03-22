package com.bybutter.sisyphus.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

object Yaml : JacksonFormatSupport() {
    override val mapper: ObjectMapper by lazy {
        ObjectMapper(YAMLFactory()).findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.IGNORE_UNDEFINED, true)
            .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
    }
}

inline fun <reified T> String?.parseYamlOrNull(): T? {
    this ?: return null

    return try {
        Yaml.deserialize(this, object : TypeReference<T>() {})
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T> String?.parseYamlOrDefault(value: T): T {
    return this.parseYamlOrNull<T>() ?: value
}

inline fun <reified T> String.parseYaml(): T =
    Yaml.deserialize(this, object : TypeReference<T>() {})

fun Any.toYaml(): String = Yaml.serialize(this)
