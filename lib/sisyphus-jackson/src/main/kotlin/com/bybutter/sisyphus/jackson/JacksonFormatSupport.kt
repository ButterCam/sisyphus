package com.bybutter.sisyphus.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import java.io.InputStream
import java.io.Reader
import java.lang.reflect.Type

abstract class JacksonFormatSupport {
    abstract val mapper: ObjectMapper

    fun <T> deserialize(
        json: String,
        type: JavaType,
    ): T {
        return mapper.readValue(json, type)
    }

    fun <T> deserialize(
        json: String,
        type: Type,
    ): T {
        return deserialize(json, mapper.constructType(type))
    }

    fun <T> deserialize(
        json: String,
        type: Class<out T>,
    ): T {
        return deserialize(json, TypeFactory.defaultInstance().constructType(type))
    }

    fun <T> deserialize(
        json: String,
        type: TypeReference<T>,
    ): T {
        return deserialize(json, TypeFactory.defaultInstance().constructType(type))
    }

    fun <T> deserialize(
        jsonReader: Reader,
        type: JavaType,
    ): T {
        return mapper.readValue(jsonReader, type)
    }

    fun <T> deserialize(
        jsonReader: Reader,
        type: Type,
    ): T {
        return deserialize(jsonReader, mapper.constructType(type))
    }

    fun <T> deserialize(
        jsonReader: Reader,
        type: Class<out T>,
    ): T {
        return deserialize(jsonReader, TypeFactory.defaultInstance().constructType(type))
    }

    fun <T> deserialize(
        jsonReader: Reader,
        type: TypeReference<T>,
    ): T {
        return deserialize(jsonReader, TypeFactory.defaultInstance().constructType(type))
    }

    fun <T> deserialize(
        stream: InputStream,
        type: JavaType,
    ): T {
        return mapper.readValue(stream, type)
    }

    fun <T> deserialize(
        stream: InputStream,
        type: Type,
    ): T {
        return deserialize(stream, mapper.constructType(type))
    }

    fun <T> deserialize(
        stream: InputStream,
        type: Class<out T>,
    ): T {
        return deserialize(stream, TypeFactory.defaultInstance().constructType(type))
    }

    fun <T> deserialize(
        stream: InputStream,
        type: TypeReference<T>,
    ): T {
        return deserialize(stream, TypeFactory.defaultInstance().constructType(type))
    }

    fun deserialize(json: String): JsonNode {
        return mapper.readTree(json)
    }

    fun deserialize(jsonParser: JsonParser): JsonNode {
        return mapper.readTree(jsonParser)
    }

    fun deserialize(jsonReader: Reader): JsonNode {
        return mapper.readTree(jsonReader)
    }

    fun deserialize(stream: InputStream): JsonNode {
        return mapper.readTree(stream)
    }

    fun serialize(`object`: Any): String {
        return mapper.writeValueAsString(`object`)
    }

    fun <T> into(
        node: TreeNode,
        type: JavaType,
    ): T {
        return mapper.readValue(mapper.treeAsTokens(node), type)
    }
}
