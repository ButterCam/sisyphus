package com.bybutter.sisyphus.protobuf.jackson.test

import com.bybutter.sisyphus.jackson.parseJson
import com.bybutter.sisyphus.jackson.parseYaml
import com.bybutter.sisyphus.jackson.toJson
import com.bybutter.sisyphus.jackson.toYaml
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FileOptions
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.protobuf.primitives.now
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ProtoTest {
    @Test
    fun `test struct json`() {
        val raw = """{"image":{"publish":false,"comment":false},"article":{"comment":false}}"""
        val result = raw.parseJson<Struct>()
        Assertions.assertEquals(raw, result.toJson())
    }

    @Test
    fun `json test`() {
        val raw = MapMessageTest {
            this.startValue = 1
            this.messageMapValue += mapOf(
                "foo" to MapMessageTest.NestedMessage {
                    this.int32Value = 1
                },
                "bar" to MapMessageTest.NestedMessage {
                    this.int32Value = 2
                }
            )
            this.baseTypeMapValue += mapOf(1 to true, 2 to false, 3 to true)
            this.endValue = 2
            this.oneTest = MapMessageTest.OneTest.StringOneofValue("test")
            this.timestamp = Timestamp.now()
            this.duration = Duration(8L, 0L, 0L)
            this.anyMapValue += mapOf(
                "foo" to MapMessageTest.NestedMessage {
                    this.int32Value = 1
                },
                "bar" to PackedTest {
                    this.values += listOf(1, 2, 3, 4, 5, 6)
                }
            )
            this.anyListValue += listOf(
                MapMessageTest.NestedMessage {
                    this.int32Value = 1
                },
                PackedTest {
                    this.values += listOf(1, 2, 3, 4, 5, 6)
                }
            )
        }

        val json = raw.toJson()
        val jsonUnmarshalled = json.parseJson<MapMessageTest>()
        Assertions.assertArrayEquals(raw.toProto(), jsonUnmarshalled.toProto())
    }

    @Test
    fun `yaml test`() {
        val raw = MapMessageTest {
            this.startValue = 1
            this.messageMapValue += mapOf(
                "foo" to MapMessageTest.NestedMessage {
                    this.int32Value = 1
                },
                "bar" to MapMessageTest.NestedMessage {
                    this.int32Value = 2
                }
            )
            this.baseTypeMapValue += mapOf(1 to true, 2 to false, 3 to true)
            this.endValue = 2
            this.oneTest = MapMessageTest.OneTest.StringOneofValue("test")
            this.timestamp = Timestamp.now()
            this.duration = Duration(8L, 0L, 0L)
            this.anyMapValue += mapOf(
                "foo" to MapMessageTest.NestedMessage {
                    this.int32Value = 1
                },
                "bar" to PackedTest {
                    this.values += listOf(1, 2, 3, 4, 5, 6)
                }
            )
            this.anyListValue += listOf(
                MapMessageTest.NestedMessage {
                    this.int32Value = 1
                },
                PackedTest {
                    this.values += listOf(1, 2, 3, 4, 5, 6)
                }
            )
        }

        val yml = raw.toYaml()
        val ymlUnmarshalled = yml.parseYaml<MapMessageTest>()
        Assertions.assertArrayEquals(raw.toProto(), ymlUnmarshalled.toProto())
    }

    @Test
    fun `extension test`() {
        val option = FileOptions {
            this.javaPackage = "test"
            this.myFileOption = "extension"
        }

        Assertions.assertEquals(option, option.toJson().parseJson<FileOptions>())
    }
}
