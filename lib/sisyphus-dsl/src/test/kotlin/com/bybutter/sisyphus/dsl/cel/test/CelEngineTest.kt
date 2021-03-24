package com.bybutter.sisyphus.dsl.cel.test

import com.bybutter.sisyphus.api.cel.test.CellTest
import com.bybutter.sisyphus.api.cel.test.HasMessageTest
import com.bybutter.sisyphus.api.cel.test.PackedTest
import com.bybutter.sisyphus.dsl.cel.CelEngine
import com.bybutter.sisyphus.dsl.cel.CelMacro
import com.bybutter.sisyphus.dsl.cel.CelRuntime
import com.bybutter.sisyphus.dsl.cel.CelStandardLibrary
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.invoke
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class CelEngineTest {
    @Test
    fun `test base cel eval`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult(""" (1 + 3) / 4 + 2 """, 3L)
        engine.assertEvalResult(""" 'this is string'.endsWith(type('')) """, true)
        engine.assertEvalResult(""" 'this is string'.matches('') """, false)
        engine.assertEvalResult(""" 'this is string'.startsWith('abc') """, false)
        engine.assertEvalResult(""" !true """, false)
        engine.assertEvalResult(""" !false """, true)
        engine.assertEvalResult(""" -(2) """, -2L)
        engine.assertEvalResult(""" 2u """, 2UL)
        engine.assertEvalResult(""" -(2.00) """, -2.0)
        engine.assertEvalResult(""" 1 == 1 """, true)
        engine.assertEvalResult(""" 1 == 0 """, false)
        engine.assertEvalResult(""" 1 % 2 """, 1L)
        engine.assertEvalResult(""" 1u % 2u """, 1UL)
        engine.assertEvalResult(""" 1 * 2 """, 2L)
        engine.assertEvalResult(""" 1.00 * 2.00 """, 2.0)
        engine.assertEvalResult(""" 1u * 2u """, 2UL)
        engine.assertEvalResult(""" 1.00 + 2.00 """, 3.0)
        engine.assertEvalResult(""" 'abc' + 'def' """, "abcdef")
        engine.assertEvalResult(""" 1u + 2u """, 3UL)
        engine.assertEvalResult(""" 3 - 2 """, 1L)
        engine.assertEvalResult(""" 3.00 - 2.00 """, 1.0)
        engine.assertEvalResult(""" 3 - 4 """, -1L)
        engine.assertEvalResult(""" 3u - 3u """, 0UL)
        engine.assertEvalResult(""" 3.00 / 3.00 """, 1.0)
        engine.assertEvalResult(""" 3u / 3u """, 1UL)
    }

    @Test
    fun `test byteArray cel eval`() {
        val engine = CelEngine(
            mapOf(
                "left" to byteArrayOf(1, 2),
                "right" to byteArrayOf(3),
                "value" to byteArrayOf()
            )
        )
        engine.assertEvalResult("""left + right""", byteArrayOf(1, 2, 3))
        engine.assertEvalResult("""left + value""", byteArrayOf(1, 2))
    }

    @Test
    fun `test list cel eval`() {
        val engine = CelEngine(
            mapOf(
                "left" to listOf<Long>(1L, 2L),
                "right" to listOf<Long>(3L),
                "value" to listOf<Long>()
            )
        )
        engine.assertEvalResult("""left + right""", listOf(1L, 2L, 3L))
        engine.assertEvalResult("""left + value""", listOf(1L, 2L))
    }

    @Test
    fun `test times cel eval`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult(
            """timestamp('2020-01-01T08:00:00.000Z') + duration('100.0s')""",
            Timestamp(2020, 1, 1, 8, 1, 40, zoneId = ZoneId.of("UTC"))
        )
        engine.assertEvalResult(
            """duration('100.0s') + timestamp('2020-01-01T08:00:00.000Z')""",
            Timestamp(2020, 1, 1, 8, 1, 40, zoneId = ZoneId.of("UTC"))
        )
        engine.assertEvalResult(
            """timestamp('2020-01-01T08:00:00.000Z') - duration('100.0s')""",
            Timestamp(2020, 1, 1, 7, 58, 20, zoneId = ZoneId.of("UTC"))
        )
        engine.assertEvalResult("""duration('100.0s') + duration('100.0s')""", Duration(200.0, TimeUnit.SECONDS))
        engine.assertEvalResult(
            """timestamp('2020-01-01T08:00:00.000Z') - timestamp('2020-01-01T07:00:00.000Z')""",
            Duration(3600.0, TimeUnit.SECONDS)
        )
        engine.assertEvalResult("""duration('100.0s') - duration('100.0s')""", Duration(0.0, TimeUnit.SECONDS))
    }

    @Test
    fun `test compare cel eval`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult("""1 > 0""", true)
        engine.assertEvalResult("""1 < 0""", false)
        engine.assertEvalResult("""0 == 0""", true)
        engine.assertEvalResult("""-1 == -1""", true)
        engine.assertEvalResult("""1u > 0u""", true)
        engine.assertEvalResult("""1u < 0u""", false)
        engine.assertEvalResult("""0u == 0u""", true)
        engine.assertEvalResult("""1.0 > 0.0""", true)
        engine.assertEvalResult("""1.0 < 0.0""", false)
        engine.assertEvalResult("""1.0 <= 0.0""", false)
        engine.assertEvalResult("""1.0 >= 0.0""", true)
        engine.assertEvalResult("""1.0 != 0.0""", true)
        engine.assertEvalResult("""0.0 != 0.0""", false)
        engine.assertEvalResult("""0.0 == 0.0""", true) //
        engine.assertEvalResult("""-1.0 == -1.0""", true) //
        engine.assertEvalResult(""" 'abc' > 'abcd'""", false)
        engine.assertEvalResult(""" 'abc' < 'abcd'""", true)
        engine.assertEvalResult(""" 'abc' == 'abcd'""", false) //
        engine.assertEvalResult(
            """timestamp('2020-01-01T08:00:00.000Z') > timestamp('2020-01-02T08:00:00.000Z') """,
            false
        )
        engine.assertEvalResult(
            """timestamp('2020-01-01T08:00:00.000Z') == timestamp('2020-01-01T08:00:00.000Z') """,
            true
        )
        engine.assertEvalResult(
            """timestamp('2020-01-01T08:00:00.000Z') < timestamp('2020-01-02T08:00:00.000Z') """,
            true
        )
        engine.assertEvalResult("""duration('100.0s') == duration('100.0s')""", true)
        engine.assertEvalResult("""duration('200.0s') > duration('100.0s')""", true)
        engine.assertEvalResult("""duration('200.0s') < duration('100.0s')""", false)
        val result = engine.eval(
            """ .sisyphus.api.cel.test.ConditionalTest {
            | condition: true
            |} """.trimMargin()
        )
        result
    }

    @Test
    fun `test byteArray compare to equal cel eval`() {
        val engine = CelEngine(
            mapOf(
                "left" to byteArrayOf(1, 2),
                "right" to byteArrayOf(1, 2)
            )
        )
        engine.assertEvalResult("""left == right""", false)
        engine.assertEvalResult("""left > right""", false)
        engine.assertEvalResult("""type(left)""", "bytes")
        engine.assertEvalResult("""size(left)""", 2L)
        engine.assertEvalResult("""string(left)""", "AQI")
    }

    @Test
    fun `test byteArray compare to greater cel eval`() {
        val engine = CelEngine(
            mapOf(
                "left" to byteArrayOf(1, 2, 3),
                "right" to byteArrayOf(1, 2)
            )
        )
        engine.assertEvalResult("""left == right""", false)
        engine.assertEvalResult("""left > right""", true)
        engine.assertEvalResult("""left < right""", false)
    }

    @Test
    fun `test conditional cel eval`() {
        val engine = CelEngine(
            mapOf(
                "value" to true,
                "value1" to 1L,
                "value2" to 3L
            )
        )
        engine.assertEvalResult("""value? value1 : value2""", 1L)

        val engine2 = CelEngine(
            mapOf(
                "value" to false,
                "value1" to 1L,
                "value2" to 3L
            )
        )
        engine2.assertEvalResult("""value? value1 : value2""", 3L)

        val engine3 = CelEngine(
            mapOf(
                "value" to false,
                "value2" to 3L
            )
        )
        engine3.assertEvalResult("""value? value1 : value2""", 3L)
    }

    @Test
    fun `test list index cel eval`() {
        val engine = CelEngine(
            mapOf(
                "value1" to listOf(1, 2),
                "value2" to 1L
            )
        )
        engine.assertEvalResult("""value1[value2]""", 2)
        engine.assertEvalResult("""size(value1)""", 2L)
    }

    @Test
    fun `test map index cel eval`() {
        val engine = CelEngine(
            mapOf(
                "value1" to mapOf(
                    "a" to 2L,
                    "b" to 10L
                ),
                "value2" to 10
            )
        )
        engine.assertEvalResult("""value1['a']""", 2L)
        engine.assertEvalResult("""size(value1)""", 2L)
    }

    @Test
    fun `test access cel eval`() {
        val engine = CelEngine(
            mapOf(
                "value1" to mapOf(
                    "a" to 1,
                    "b" to 3
                ),
                "value2" to 1
            )
        )
        engine.assertEvalResult("""value1.a""", 1)
    }

    @Test
    fun `test map key membership and type conversion `() {
        val engine = CelEngine(
            mapOf(
                "value1" to mapOf(
                    "a" to 1,
                    "b" to 2
                ),
                "value2" to "a"
            )
        )
        engine.assertEvalResult("""value2 in value1""", true)
        engine.assertEvalResult("""type(value1)""", "map")
    }

    @Test
    fun `test map not contains cel eval`() {
        val engine = CelEngine(
            mapOf(
                "map" to mapOf(
                    "a" to 1,
                    "b" to 2
                ),
                "key" to "c"
            )
        )
        engine.assertEvalResult("""key in map""", false)
    }

    @Test
    fun `test list contains cel eval`() {
        val engine = CelEngine(
            mapOf(
                "value1" to listOf(1L, 2L, 3L)
            )
        )
        engine.assertEvalResult("""type(value1)""", "list")
        engine.assertEvalResult("""1 in value1""", true)
        engine.assertEvalResult("""4 in value1""", false)
    }

    @Test
    fun `test string contains cel eval`() {
        val engine = CelEngine(
            mapOf(
                "other" to "abc"
            )
        )
        engine.assertEvalResult("""'abcd'.contains(other)""", true)
    }

    @Test
    fun `test logicalOr cel eval`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult(""" true || false """, true)
        engine.assertEvalResult(""" false || true """, true)
        engine.assertEvalResult(""" true || true """, true)
        engine.assertEvalResult(""" false || false """, false)
    }

    @Test
    fun `test logicalAnd cel eval`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult(""" true && false """, false)
        engine.assertEvalResult(""" false && true """, false)
        engine.assertEvalResult(""" true && true """, true)
        engine.assertEvalResult(""" false && false """, false)
    }

    @Test
    fun `test double type conversion`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult("""bytes('abcd')""", byteArrayOf(97, 98, 99, 100))
        engine.assertEvalResult("""double(12345678901)""", 12345678901.0)
        engine.assertEvalResult("""double(-12345678901)""", -12345678901.0)
        engine.assertEvalResult("""double(12345678901u)""", 12345678901.0)
        engine.assertEvalResult("""double('0.0')""", 0.0)
    }

    @Test
    fun `test type denotation`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult("""type(12345678901)""", "int")
        engine.assertEvalResult("""type(123u)""", "uint")
        engine.assertEvalResult("""type(1.0)""", "double")
        engine.assertEvalResult("""type(null)""", "null_type")
        engine.assertEvalResult("""type(timestamp('2020-01-01T08:00:00.000Z'))""", ".google.protobuf.Timestamp")
    }

    @Test
    fun `test get time from the date`() {
        val engine = CelEngine(
            mapOf(
                "value" to Timestamp(2020, 1, 1, 8, 1, 1, 999, zoneId = ZoneId.of("UTC")),
                "value2" to Duration(3600.0, TimeUnit.SECONDS),
                "zone" to "Z"
            )
        )
        engine.assertEvalResult("""value.getDayOfMonth()""", 0L)
        engine.assertEvalResult("""value.getDayOfMonth(zone)""", 0L)
        engine.assertEvalResult("""value.getDayOfWeek()""", 3L)
        engine.assertEvalResult("""value.getDayOfYear()""", 0L)
        engine.assertEvalResult("""value.getDayOfYear(zone)""", 0L)
        engine.assertEvalResult("""value.getFullYear()""", 2020L)
        engine.assertEvalResult("""value.getFullYear(zone)""", 2020L)
        engine.assertEvalResult("""value.getHours()""", 8L)
        engine.assertEvalResult("""value.getHours(zone)""", 8L)
        engine.assertEvalResult("""value2.getHours()""", 1L)
        engine.assertEvalResult("""value.getMilliseconds()""", 0L)
        engine.assertEvalResult("""value.getMilliseconds(zone)""", 0L)
        engine.assertEvalResult("""value2.getMilliseconds()""", 0L)
        engine.assertEvalResult("""value.getMinutes()""", 1L)
        engine.assertEvalResult("""value.getMinutes(zone)""", 1L)
        engine.assertEvalResult("""value2.getMinutes()""", 60L)
        engine.assertEvalResult("""value.getMonth()""", 0L)
        engine.assertEvalResult("""value.getSeconds()""", 1L)
        engine.assertEvalResult("""value.getSeconds(zone)""", 1L)
        engine.assertEvalResult("""value2.getSeconds()""", 3600L)
    }

    @Test
    fun `test Long type conversion`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult("""int(123u)""", 123L)
        engine.assertEvalResult("""int(123.0)""", 123L)
        engine.assertEvalResult("""int("123")""", 123L)
        engine.assertEvalResult("""int(timestamp('2020-01-01T08:00:00.000Z'))""", 1577865600L)
    }

    @Test
    fun `test get size of value`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult("""size("123")""", 3L)
    }

    @Test
    fun `test string type conversion`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult("""string(12)""", "12")
        engine.assertEvalResult("""string(12u)""", "12")
        engine.assertEvalResult("""string(12.0)""", "12.0")
    }

    @Test
    fun `test uint type conversion`() {
        val engine = CelEngine(mapOf())
        engine.assertEvalResult("""uint(12)""", 12UL)
        engine.assertEvalResult("""uint(12.0)""", 12UL)
        engine.assertEvalResult("""uint('12')""", 12UL)
    }

    @Test
    fun `test cel with global`() {
        val engine = CelEngine(mapOf(), CelRuntime(std = CustomCelStandardLibrary()))
        val global = mapOf<String, Any?>(
            "a" to 1,
            "b" to 1.0,
            "c" to 1L,
            "d" to 1u,
            "e" to "xyz"
        )
        val result1 = engine.eval("""'a'""", global)
        val result2 = engine.eval("""type('a')""", global)
        Assertions.assertEquals(result1, "a")
        Assertions.assertEquals(result2, "string")
    }

    @Test
    fun `test cel left and right eval2`() {
        val engine = CelEngine(
            mapOf(
                "raw" to CellTest {
                    left = 1uL
                    right = 2uL
                }
            )
        )
        engine.assertEvalResult("""raw.left + raw.right""", 3uL)
    }

    @Test
    fun `test custom stdlib`() {
        val engine = CelEngine(mapOf(), CelRuntime(std = CustomCelStandardLibrary()))
        val result = engine.eval("""test() + 2""")
        Assertions.assertEquals(result, 1L)
    }

    @Test
    fun `test List CelRuntime with macro`() {
        val engine = CelEngine(
            mapOf(
                "value" to listOf(1L, 3L)
            ),
            CelRuntime(macro = CustomCelMacro())
        )
        val result = engine.eval("""value.all(x, x % 2 == 1)""")
        val result2 = engine.eval("""value.all(x, x == 1)""")
        val result3 = engine.eval("""value.exists(x,x >= 1)""")
        val result4 = engine.eval("""value.exists(x,x > 1 && x < -1)""")
        val result5 = engine.eval("""value.exists_one(x,x + 1 == 2)""")
        val result6 = engine.eval("""value.exists_one(x,x > 3)""")
        val result7 = engine.eval("""value.map(x,x == 1)""")
        val result8 = engine.eval("""value.map(x,x * x)""")
        val result9 = engine.eval("""value.filter(x,x % 2 > 0)""")
        val result10 = engine.eval("""value.filter(x,x >= 3)""")
        Assertions.assertEquals(result, true)
        Assertions.assertEquals(result2, false)
        Assertions.assertEquals(result3, true)
        Assertions.assertEquals(result4, false)
        Assertions.assertEquals(result5, true)
        Assertions.assertEquals(result6, false)
        Assertions.assertEquals(result7, arrayListOf(true, false))
        Assertions.assertEquals(result8, arrayListOf(1L, 9L))
        Assertions.assertEquals(result9, arrayListOf(1L, 3L))
        Assertions.assertEquals(result10, arrayListOf(3L))
    }

    @Test
    fun `test map CelRuntime with macro`() {
        val engine = CelEngine(
            mapOf(
                "value" to HasMessageTest {
                    this.startValue = 1
                    this.messageMapValue += mapOf(
                        "foo" to HasMessageTest.HasMessage {
                            this.int32Value = 1
                        },
                        "bar" to HasMessageTest.HasMessage {
                            this.int32Value = 2
                        },
                        "a" to HasMessageTest.HasMessage {
                            this.int32Value = 3
                        },
                        "b" to HasMessageTest.HasMessage {
                            this.int32Value = 4
                        }
                    )
                    this.baseTypeMapValue += mapOf(1 to true, 2 to false, 3 to true)
                    this.endValue = 2
                    this.anyMapValue += mapOf(
                        "foo" to HasMessageTest.HasMessage {
                            this.int32Value = 1
                        },
                        "bar" to PackedTest {
                            this.values += listOf(1, 2, 3, 4, 5, 6)
                        }
                    )
                    this.anyListValue += listOf(
                        HasMessageTest.HasMessage {
                            this.int32Value = 1
                        },
                        PackedTest {
                            this.values += listOf(1, 2, 3, 4, 5, 6)
                        }
                    )
                }
            ),
            CelRuntime(macro = CustomCelMacro())
        )
        val result = engine.eval("""value.anyMapValue.all(x,x == 1)""")
        val result2 = engine.eval("""value.anyMapValue.all(x,x != 3)""")
        val result3 = engine.eval("""has(value.messageMapValue.foo)""")
        val result4 = engine.eval("""has(value.messageMapValue.ba)""")
        val result5 = engine.eval("""value.messageMapValue.exists(x,x == 'a')""")
        val result6 = engine.eval("""value.messageMapValue.exists(x,x == 5)""")
        val result7 = engine.eval("""value.messageMapValue.exists_one(x,x == 'foo')""")
        val result8 = engine.eval("""value.messageMapValue.exists_one(x,x == 9)""")
        Assertions.assertEquals(result, false)
        Assertions.assertEquals(result2, true)
        Assertions.assertEquals(result3, true)
        Assertions.assertEquals(result4, false)
        Assertions.assertEquals(result5, true)
        Assertions.assertEquals(result6, false)
        Assertions.assertEquals(result7, true)
        Assertions.assertEquals(result8, false)
    }

    class CustomCelMacro : CelMacro()

    class CustomCelStandardLibrary : CelStandardLibrary() {
        fun test(): Long {
            return 1L
        }

        override fun plus(left: Long, right: Long): Long {
            return left
        }
    }

    private fun CelEngine.assertEvalResult(script: String, result: Any?) {
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
