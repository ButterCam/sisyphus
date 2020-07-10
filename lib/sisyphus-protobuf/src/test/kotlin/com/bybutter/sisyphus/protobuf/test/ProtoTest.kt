package com.bybutter.sisyphus.protobuf.test

import com.bybutter.sisyphus.jackson.parseJson
import com.bybutter.sisyphus.jackson.parseYaml
import com.bybutter.sisyphus.jackson.toJson
import com.bybutter.sisyphus.jackson.toYaml
import com.bybutter.sisyphus.protobuf.MessagePatcher
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FileOptions
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.protobuf.primitives.now
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ProtoTest {
    @Test
    fun `base type marshal and unmarshal test`() {
        val raw = BaseTypeTest {
            stringValue = "test"
            int32Value = -1
            int64Value = -100
        }

        val unmarshlled = BaseTypeTest.parse(raw.toProto())
        Assertions.assertArrayEquals(raw.toProto(), unmarshlled.toProto())
    }

    @Test
    fun `nested message marshal and unmarshal test`() {
        val raw = NestedMessageTest {
            this.startValue = 1
            this.nestedMessageValue = NestedMessageTest.NestedMessage {
                this.int32Value = 3
            }
            this.endValue = 2
        }

        val data = raw.toProto()
        val unmarshlled = NestedMessageTest.parse(data)
        Assertions.assertArrayEquals(raw.toProto(), unmarshlled.toProto())
    }

    @Test
    fun `map message marshal and unmarshal test`() {
        val raw = MapMessageTest {
            this.startValue = 1
            this.messageMapValue += mapOf(
                "foo" to MapMessageTest.NestedMessage {
                    this.int32Value = 1
                },
                "bar" to MapMessageTest.NestedMessage {
                    this.int32Value = 2
                })
            this.baseTypeMapValue += mapOf(1 to true, 2 to false, 3 to true)
            this.endValue = 2
        }

        val data = raw.toProto()
        val unmarshlled = MapMessageTest.parse(data)
        Assertions.assertArrayEquals(raw.toProto(), unmarshlled.toProto())
    }

//    @Test
//    fun `test field mask`() {
//        val raw = NestedMessageTest {
//            this.startValue = 1
//            this.nestedMessageValue = NestedMessageTest.NestedMessage {
//                this.int32Value = 3
//            }
//            this.endValue = 2
//        }
//
//        val masked = raw * FieldMask("start_value")
//        Assertions.assertEquals(raw.startValue, masked.startValue)
//        Assertions.assertNull(masked.nestedMessageValue)
//        Assertions.assertEquals(0, masked.endValue)
//    }

    @Test
    fun `test packed field`() {
        val raw = PackedTest {
            this.values += listOf(1, 2, 3, 4, 5, 6)
        }

        val proto = PackedTest.parse(raw.toProto())
        Assertions.assertArrayEquals(proto.toProto(), raw.toProto())
    }

    @Test
    fun `test struct`() {
        val raw = Struct {
            fields += mapOf(
                "test" to Value {
                    kind = Value.Kind.StringValue("value")
                }
            )
        }
        Assertions.assertArrayEquals(raw.toProto(), Struct.parse(raw.toProto()).toProto())
    }

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
                })
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
        val jsonUnmarshlled = json.parseJson<MapMessageTest>()
        Assertions.assertArrayEquals(raw.toProto(), jsonUnmarshlled.toProto())
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
                })
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
        val ymlUnmarshlled = yml.parseYaml<MapMessageTest>()
        Assertions.assertArrayEquals(raw.toProto(), ymlUnmarshlled.toProto())
    }

    @Test
    fun `extension test`() {
        val option = FileOptions {
            this.javaPackage = "test"
            this.myFileOption = "extension"
        }

        val json = option.toJson()
        val jsonUnmarshlled = json.parseJson<FileOptions>()
        Assertions.assertArrayEquals(option.toProto(), jsonUnmarshlled.toProto())
        Assertions.assertArrayEquals(option.toProto(), FileOptions.parse(option.toProto()).toProto())
    }

    @Test
    fun `message patcher test`() {
        val patcher = MessagePatcher()
        patcher.add("start_value", "2")
        patcher.add("nested_message_value.int32_value", "3")
        patcher.add("end_value", "4")

        val raw = NestedMessageTest {
            patcher.applyTo(this)
        }

        Assertions.assertEquals(raw.startValue, 2)
        Assertions.assertEquals(raw.nestedMessageValue?.int32Value, 3)
        Assertions.assertEquals(raw.endValue, 4)
    }
}
