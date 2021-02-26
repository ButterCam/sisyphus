package com.bybutter.sisyphus.dsl.cel.test

import com.bybutter.sisyphus.api.cel.test.HasMessageTest
import com.bybutter.sisyphus.dsl.filtering.FilterEngine
import com.bybutter.sisyphus.dsl.filtering.MessageFilter
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.ListValue
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.invoke
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FilterEngineTest {
    @Test
    fun `test base filter eval`() {
        val engine = FilterEngine(
            mapOf(
                "a" to 1,
                "b" to mapOf("c" to 1, "c2" to arrayListOf(1, 2, 3)),
                "d" to 1.0,
                "e" to "e",
                "f" to 1.0f,
                "g" to Duration(1),
                "h" to Timestamp("2021-02-26T13:25:00Z")
            )
        )

        engine.assertEvalResult("a = 1", true)
        engine.assertEvalResult("a = 2", false)
        engine.assertEvalResult("a < 2", true)
        engine.assertEvalResult("a != 2", true)
        engine.assertEvalResult("a != 1", false)

        engine.assertEvalResult("b : 'c'", true)
        engine.assertEvalResult("b.c2 : 1", true)
        engine.assertEvalResult("b.c = 1", true)

        engine.assertEvalResult("a = 1 AND b.c = 1", true)
        engine.assertEvalResult("a = 1 AND b.c = 2", false)
        engine.assertEvalResult("a = 1 OR b.c = 2", true)
        engine.assertEvalResult("a = 2 OR b.c = 2", false)

        engine.assertEvalResult("a = 1 b.c = 1", true)
        engine.assertEvalResult("a = 1 b.c = 2", false)

        engine.assertEvalResult("d > 1.0", false)
        engine.assertEvalResult("d >= 1.0", false)
        engine.assertEvalResult("e > 'a'", true)
        engine.assertEvalResult("f < 2.0", true)
        engine.assertEvalResult("f <= 2.0", true)
        engine.assertEvalResult("NOT f < 2.0", false)
        engine.assertEvalResult("g < 2s", true)
        engine.assertEvalResult("h = 2021-02-26T13:25:00Z", true)
        engine.assertEvalResult("h < 2021-02-26T13:30:00Z", true)
    }

    @Test
    fun `test message filter eval`() {
        val message = HasMessageTest {
            this.startValue = 10
            this.hasMessageValue = HasMessageTest.HasMessage {
                this.int32Value = 20
            }
            this.value = Value {
                this.numberValue = 30.0
            }
            this.valueList = Value {
                this.listValue = ListValue {
                    this.values += arrayListOf(Value {
                        this.numberValue = 30.0
                    })
                }
            }
            this.valueStruct = Value {
                this.structValue = Struct {
                    this.fields += mapOf("struct" to Value {
                        this.numberValue = 30.0
                    })
                }
            }
            this.valueString = Value {
                this.stringValue = "a"
            }
        }
        message.assertFilterEvalResult("startValue = 10", true)
        message.assertFilterEvalResult("startValue < 10", false)
        message.assertFilterEvalResult("hasMessageValue.int32Value = 20", true)
        message.assertFilterEvalResult("value = 30.0", true)
        message.assertFilterEvalResult("valueList : 30.0", true)
        message.assertFilterEvalResult("hasMessageValue : 'int32Value'", true)
        message.assertFilterEvalResult("valueString = 'a'", true)
        message.assertFilterEvalResult("valueStruct.fields : 'struct'", true)
    }

    private fun Message<*, *>.assertFilterEvalResult(script: String, result: Boolean) {
        Assertions.assertEquals(result, MessageFilter(script).filter(this))
    }

    private fun FilterEngine.assertEvalResult(script: String, result: Any?) {
        val out = eval(script)
        if (out?.javaClass?.isArray == true && result?.javaClass == out.javaClass) {
            when (out) {
                is ByteArray -> {
                    Assertions.assertArrayEquals(out, result as ByteArray)
                }
                is Array<*> -> {
                    Assertions.assertArrayEquals(out, result as Array<*>)
                }
            }
        } else {
            Assertions.assertEquals(eval(script), result)
        }
    }
}
