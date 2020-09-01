package com.bybutter.sisyphus.test.test

import com.bybutter.sisyphus.test.JsonSource
import com.bybutter.sisyphus.test.TestCase
import com.bybutter.sisyphus.test.run
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest

class Test {

    @ParameterizedTest
    @JsonSource("""
        {
            "serviceTestSet": [
                {
                    "service": "bybutter.xxxx.XxXApi",
                    "methodTestSet": [
                        {
                            "method": "Echo",
                            "input": {
                                "@type": "types.bybutter.com/bybutter.xxxx.FooBar",
                                "test": 1
                            },
                            "asserts": [
                                "request.test == response.test"
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