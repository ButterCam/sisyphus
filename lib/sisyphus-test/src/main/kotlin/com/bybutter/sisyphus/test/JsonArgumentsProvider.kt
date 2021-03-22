package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.jackson.parseJson
import java.util.stream.Stream
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer

class JsonArgumentsProvider : AnnotationConsumer<JsonSource>, ArgumentsProvider {
    private lateinit var cases: Array<out String>

    override fun accept(jsonSource: JsonSource) {
        cases = jsonSource.cases
    }

    override fun provideArguments(extensionContext: ExtensionContext): Stream<out Arguments> {
        return cases.map { Arguments.of(it.parseJson<TestCase>()) }.stream()
    }
}