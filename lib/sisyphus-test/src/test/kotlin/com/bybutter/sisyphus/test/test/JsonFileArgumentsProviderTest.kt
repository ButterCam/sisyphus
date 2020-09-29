package com.bybutter.sisyphus.test.test

import com.bybutter.sisyphus.test.JsonFileSource
import com.bybutter.sisyphus.test.TestCase
import com.bybutter.sisyphus.test.run
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest

@SpringBootApplication
@SpringBootTest
class JsonFileArgumentsProviderTest {
    @ParameterizedTest
    @JsonFileSource(resources = ["json-file-test.json"])
    @DisplayName("json file test")
    fun test(case: TestCase) {
        case.run()
    }
}