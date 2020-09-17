package com.bybutter.sisyphus.test.test

import com.bybutter.sisyphus.test.JsonSource
import com.bybutter.sisyphus.test.TestCase
import com.bybutter.sisyphus.test.run
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.RestAssured
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest

class JsonArgumentsProviderTest {
    var wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(8089)
        wireMockServer!!.start()
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
        configureFor("127.0.0.1", 8089)
        wireMockServer!!.stubFor(get(urlEqualTo("/sisyphus/butter/test"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("mappings/mockPostService.json")))
    }

    @ParameterizedTest
    @JsonSource("""
        {
            "metadata": {
                "host": "127.0.0.1:8089",
                "Content-Type": "application/json"
            },
            "serviceTestSet": [
                {
                    "service": "/sisyphus/butter/test",
                    "methodTests": [
                        {
                            "name":"Json Arguments Provider Test name",
                            "title":"Json Arguments Provider Test title"
                            "method": "Echo",
                            "input": {
                                "@type": "types.bybutter.com/bybutter.xxxx.FooBar",
                                "test": 1
                            },
                            "asserts": [
                                "response.status == 200"
                            ]
                        }
                    ]
                }
            ]
        }
    """)
    @DisplayName("first test")
    fun test(case: TestCase) {
        case.run()
    }
}