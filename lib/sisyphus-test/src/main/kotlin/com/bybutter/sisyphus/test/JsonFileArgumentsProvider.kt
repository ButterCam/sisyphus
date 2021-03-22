package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.jackson.Json
import java.util.stream.Stream
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer

class JsonFileArgumentsProvider : AnnotationConsumer<JsonFileSource>, ArgumentsProvider {
    private lateinit var resources: Array<out String>

    override fun accept(jsonFileSource: JsonFileSource) {
        resources = jsonFileSource.resources
    }

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        return resources.flatMap {
            JsonFileArgumentsProvider::class.java.classLoader.getResources(it).toList()
        }.map {
            it.openStream().use {
                Json.deserialize(it, TestCase::class.java)
            }
        }.map {
            Arguments.of(it)
        }.stream()
    }
}