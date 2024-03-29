package com.bybutter.sisyphus.protobuf.test

import com.bybutter.sisyphus.jackson.Json
import com.bybutter.sisyphus.jackson.parseJson
import com.bybutter.sisyphus.jackson.parseYaml
import com.bybutter.sisyphus.jackson.toJson
import com.bybutter.sisyphus.jackson.toYaml
import com.bybutter.sisyphus.protobuf.LocalProtoReflection
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.dynamic.DynamicFileSupport
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.protobuf.gson.registerProtobufAdapterFactory
import com.bybutter.sisyphus.protobuf.invoke
import com.bybutter.sisyphus.protobuf.jackson.JacksonReader
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FieldMask
import com.bybutter.sisyphus.protobuf.primitives.FileOptions
import com.bybutter.sisyphus.protobuf.primitives.ListValue
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.field
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.protobuf.primitives.list
import com.bybutter.sisyphus.protobuf.primitives.now
import com.bybutter.sisyphus.protobuf.primitives.struct
import com.bybutter.sisyphus.protobuf.primitives.value
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JsonTest {
    private val gson = GsonBuilder().registerProtobufAdapterFactory().create()

    @Test
    fun `test struct json`() {
        val raw =
            """{"image":{"publish":false,"comment":false},"list":[{"comment":false},{"comment":false}],"article":{"comment":false}}"""
        val result = raw.parseJson<Struct>()
        Assertions.assertEquals(raw, result.toJson())
        Assertions.assertEquals(raw, gson.toJson(result))
    }

    @Test
    fun `json test`() {
        val raw =
            MapMessageTest {
                this.startValue = 1
                this.messageMapValue +=
                    mapOf(
                        "foo" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 1
                            },
                        "bar" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 2
                            },
                    )
                this.baseTypeMapValue += mapOf(1 to true, 2 to false, 3 to true)
                this.endValue = 2
                this.oneTest = MapMessageTest.OneTest.StringOneofValue("test")
                this.timestamp = Timestamp.now()
                this.duration = Duration(8L, 0L, 0L)
                this.anyMapValue +=
                    mapOf(
                        "foo" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 1
                            },
                        "bar" to
                            PackedTest {
                                this.values += listOf(1, 2, 3, 4, 5, 6)
                            },
                    )
                this.anyListValue +=
                    listOf(
                        MapMessageTest.NestedMessage {
                            this.int32Value = 1
                        },
                        PackedTest {
                            this.values += listOf(1, 2, 3, 4, 5, 6)
                        },
                    )
            }

        val json = raw.toJson()
        val jsonUnmarshalled = json.parseJson<MapMessageTest>()
        val gsonUnmarshalled = gson.fromJson(json, MapMessageTest::class.java)
        Assertions.assertArrayEquals(raw.toProto(), jsonUnmarshalled.toProto())
        Assertions.assertArrayEquals(raw.toProto(), gsonUnmarshalled.toProto())
    }

    @Test
    fun `yaml test`() {
        val raw =
            MapMessageTest {
                this.startValue = 1
                this.messageMapValue +=
                    mapOf(
                        "foo" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 1
                            },
                        "bar" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 2
                            },
                    )
                this.baseTypeMapValue += mapOf(1 to true, 2 to false, 3 to true)
                this.endValue = 2
                this.oneTest = MapMessageTest.OneTest.StringOneofValue("test")
                this.timestamp = Timestamp.now()
                this.duration = Duration(8L, 0L, 0L)
                this.anyMapValue +=
                    mapOf(
                        "foo" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 1
                            },
                        "bar" to
                            PackedTest {
                                this.values += listOf(1, 2, 3, 4, 5, 6)
                            },
                    )
                this.anyListValue +=
                    listOf(
                        MapMessageTest.NestedMessage {
                            this.int32Value = 1
                        },
                        PackedTest {
                            this.values += listOf(1, 2, 3, 4, 5, 6)
                        },
                    )
            }

        val yml = raw.toYaml()
        val ymlUnmarshalled = yml.parseYaml<MapMessageTest>()
        Assertions.assertArrayEquals(raw.toProto(), ymlUnmarshalled.toProto())
    }

    @Test
    fun `extension test`() {
        val option =
            FileOptions {
                this.javaPackage = "test"
                this.myFileOption = "extension"
            }

        Assertions.assertEquals(option, option.toJson().parseJson<FileOptions>())
        Assertions.assertEquals(option, gson.fromJson(gson.toJson(option), FileOptions::class.java))
    }

    @Test
    fun `custom proto type test`() {
        val test1 =
            ResourceNameTest {
                this.name = ResourceNameTest.Name.of("1")
            }
        val test2 =
            ResourceNameTest2 {
                this.test = ResourceNameTest.Name.of("1")
            }

        Assertions.assertEquals(test1, test1.toJson().parseJson<ResourceNameTest>())
        Assertions.assertEquals(test1, gson.fromJson(gson.toJson(test1), ResourceNameTest::class.java))
        Assertions.assertEquals(test2, test2.toJson().parseJson<ResourceNameTest2>())
        Assertions.assertEquals(test2, gson.fromJson(gson.toJson(test2), ResourceNameTest2::class.java))
    }

    @Test
    fun `list test`() {
        val raw =
            MapMessageTest {
                this.startValue = 1
                this.messageMapValue +=
                    mapOf(
                        "foo" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 1
                            },
                        "bar" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 2
                            },
                    )
                this.baseTypeMapValue += mapOf(1 to true, 2 to false, 3 to true)
                this.endValue = 2
                this.oneTest = MapMessageTest.OneTest.StringOneofValue("test")
                this.timestamp = Timestamp.now()
                this.duration = Duration(8L, 0L, 0L)
                this.anyMapValue +=
                    mapOf(
                        "foo" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 1
                            },
                        "bar" to
                            PackedTest {
                                this.values += listOf(1, 2, 3, 4, 5, 6)
                            },
                    )
                this.anyListValue +=
                    listOf(
                        MapMessageTest.NestedMessage {
                            this.int32Value = 1
                        },
                        PackedTest {
                            this.values += listOf(1, 2, 3, 4, 5, 6)
                        },
                        Value {
                            this.stringValue = "test"
                        },
                        Value {
                            this.boolValue = false
                        },
                        Value {
                            this.numberValue = 123.456
                        },
                        Value {
                            this.listValue =
                                ListValue {
                                    value("test")
                                    value(1.0)
                                    struct {
                                        field("string", "test")
                                        field("number", 2.0)
                                    }
                                    list {
                                        value("test")
                                        value(3.0)
                                    }
                                }
                        },
                        Timestamp.now(), Duration(1.234), FieldMask("test1", "foo", "bar"),
                    )
            }

        val json = listOf(raw, raw).toJson()
        Assertions.assertEquals(json, json.parseJson<List<MapMessageTest>>().toJson())
        Assertions.assertEquals(
            json,
            gson.toJson(gson.fromJson(json, object : TypeToken<List<MapMessageTest>>() {}.type) as List<MapMessageTest>),
        )
    }

    @Test
    fun `unknown field`() {
        val json = """{"string_value": "", "test": "", "test2": [{}, {}], "test3": {"test": []}, "int64_value": 10}"""

        val jsonUnmarshalled = json.parseJson<BaseTypeTest>()
        val gsonUnmarshalled = gson.fromJson(json, BaseTypeTest::class.java)
        Assertions.assertArrayEquals(gsonUnmarshalled.toProto(), jsonUnmarshalled.toProto())
    }

    @Test
    fun `dynamic test`() {
        val raw =
            MapMessageTest {
                this.startValue = 1
                this.messageMapValue +=
                    mapOf(
                        "foo" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 1
                            },
                        "bar" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 2
                            },
                    )
                this.baseTypeMapValue += mapOf(1 to true, 2 to false, 3 to true)
                this.endValue = 2
                this.oneTest = MapMessageTest.OneTest.StringOneofValue("test")
                this.timestamp = Timestamp.now()
                this.duration = Duration(8L, 0L, 0L)
                this.anyMapValue +=
                    mapOf(
                        "foo" to
                            MapMessageTest.NestedMessage {
                                this.int32Value = 1
                            },
                        "bar" to
                            PackedTest {
                                this.values += listOf(1, 2, 3, 4, 5, 6)
                            },
                    )
                this.anyListValue +=
                    listOf(
                        MapMessageTest.NestedMessage {
                            this.int32Value = 1
                        },
                        PackedTest {
                            this.values += listOf(1, 2, 3, 4, 5, 6)
                        },
                        Value {
                            this.stringValue = "test"
                        },
                        Value {
                            this.boolValue = false
                        },
                        Value {
                            this.numberValue = 123.456
                        },
                        Value {
                            this.listValue =
                                ListValue {
                                    value("test")
                                    value(1.0)
                                    struct {
                                        field("string", "test")
                                        field("number", 2.0)
                                    }
                                    list {
                                        value("test")
                                        value(3.0)
                                    }
                                }
                        },
                        Timestamp.now(), Duration(1.234), FieldMask("test1", "foo", "bar"),
                    )
            }
        val json = raw.toJson()

        val dynamicSupport =
            LocalProtoReflection().apply {
                ProtoTypes.files().forEach {
                    register(DynamicFileSupport(it.descriptor))
                }
            }

        val dynamicJson =
            dynamicSupport {
                dynamicSupport.findMessageSupport(MapMessageTest.name).invoke {
                    val reader = JacksonReader(Json.mapper.createParser(json))
                    reader.next()
                    readFrom(reader)
                }.toJson()
            }
        Assertions.assertEquals(json, dynamicJson)
    }
}
