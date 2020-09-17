package com.bybutter.sisyphus.test.test

import com.bybutter.sisyphus.test.JsonFileSource
import com.bybutter.sisyphus.test.TestCase
import com.bybutter.sisyphus.test.run
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.restassured.RestAssured
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest

class JsonFileArgumentsProviderTest {

    var wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
        wireMockServer?.start()
        RestAssured.baseURI = "http://127.0.0.1"
        RestAssured.port = 8089
        setupStub()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer?.stop()
//        RestAssured.reset()
    }

    private fun setupStub() {
        wireMockServer!!.stubFor(WireMock.get(WireMock.urlEqualTo("/sisyphus/butter/test"))
                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("\"testing\": \"butter\"")))
    }

    @ParameterizedTest
    @JsonFileSource(resources = ["json-file-test.json"])
    @DisplayName("json file test")
    fun test(case: TestCase) {
        case.run()
    }
}