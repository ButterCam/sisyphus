package com.bybutter.sisyphus.protobuf.compiler.generator.basic

import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.generating.ExtensionFieldGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.FileGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ApiGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.SupportGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.className
import com.bybutter.sisyphus.protobuf.compiler.generating.clearFunction
import com.bybutter.sisyphus.protobuf.compiler.generating.compiler
import com.bybutter.sisyphus.protobuf.compiler.generating.defaultValue
import com.bybutter.sisyphus.protobuf.compiler.generating.document
import com.bybutter.sisyphus.protobuf.compiler.generating.extendeeClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.extendeeMutableClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.fieldType
import com.bybutter.sisyphus.protobuf.compiler.generating.fileMetadataClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.fullProtoName
import com.bybutter.sisyphus.protobuf.compiler.generating.hasFunction
import com.bybutter.sisyphus.protobuf.compiler.generating.mapEntry
import com.bybutter.sisyphus.protobuf.compiler.generating.mutableFieldType
import com.bybutter.sisyphus.protobuf.compiler.generating.name
import com.bybutter.sisyphus.protobuf.compiler.generating.propertyMemberName
import com.bybutter.sisyphus.protobuf.compiler.generating.supportName
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.kFun
import com.bybutter.sisyphus.protobuf.compiler.kObject
import com.bybutter.sisyphus.protobuf.compiler.kProperty
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.bybutter.sisyphus.protobuf.compiler.setter
import com.bybutter.sisyphus.protobuf.compiler.util.makeTag
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.WireFormat
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KProperty

open class ExtensionFieldGenerator : UniqueGenerator<ExtensionFieldGenerating<*, *>> {
    override fun generate(state: ExtensionFieldGenerating<*, *>): Boolean {
        if (state !is ApiGenerating) return false
        val property = kProperty(state.descriptor.jsonName, state.fieldType()) {
            receiver(state.extendeeClassName())
            addKdoc(state.document())
            if (state.descriptor.options?.deprecated == true) {
                annotation(Deprecated::class.asClassName()) {
                    addMember("message = %S", "${state.name()} has been marked as deprecated")
                }
            }
            getter {
                addStatement("return this[${state.descriptor.number}]")
            }
        }

        val hasFunc = kFun(state.hasFunction()) {
            receiver(state.extendeeClassName())
            returns(Boolean::class)
            addStatement("return this.has(${state.descriptor.number})")
        }

        val mutableProperty = kProperty(state.descriptor.jsonName, state.fieldType()) {
            receiver(state.extendeeMutableClassName())
            mutable(true)
            addKdoc(state.document())
            if (state.descriptor.options?.deprecated == true) {
                annotation(Deprecated::class.asClassName()) {
                    addMember("message = %S", "${state.name()} has been marked as deprecated")
                }
            }
            getter {
                addStatement("return this[${state.descriptor.number}]")
            }
            setter {
                addParameter("value", state.fieldType())
                addStatement("this[${state.descriptor.number}] = value")
            }
        }

        val clearFunc = kFun(state.clearFunction()) {
            receiver(state.extendeeMutableClassName())
            returns(state.fieldType().copy(true))
            addStatement("return this.clear(${state.descriptor.number}) as %T", state.fieldType().copy(true))
        }

        when (val target = state.target) {
            is FileSpec.Builder -> {
                target.addProperty(property)
                target.addProperty(mutableProperty)
                target.addFunction(hasFunc)
                target.addFunction(clearFunc)
            }
            is TypeSpec.Builder -> {
                target.addProperty(property)
                target.addProperty(mutableProperty)
                target.addFunction(hasFunc)
                target.addFunction(clearFunc)
            }
        }

        return true
    }
}

open class ExtensionFieldSupportGenerator : UniqueGenerator<ExtensionFieldGenerating<*, *>> {
    override fun generate(state: ExtensionFieldGenerating<*, *>): Boolean {
        if (state !is SupportGenerating) return false

        val support = kObject(state.supportName()) {
            val fieldType = state.mutableFieldType().copy(false)
            this extends RuntimeTypes.EXTENSION_SUPPORT.parameterizedBy(fieldType)

            property("name", String::class) {
                this += KModifier.OVERRIDE
                getter {
                    when (val parent = state.parent) {
                        is MessageGenerating<*, *> -> {
                            addStatement(
                                "return %S",
                                "${state.descriptor.extendee.trim('.')}.${state.descriptor.name}@${parent.fullProtoName()}"
                            )
                        }
                        is FileGenerating<*, *> -> {
                            addStatement(
                                "return %S",
                                "${state.descriptor.extendee.trim('.')}.${state.descriptor.name}@${parent.descriptor.`package`}"
                            )
                        }
                    }
                }
            }

            when (val parent = state.parent) {
                is MessageGenerating<*, *> -> {
                    property("parent", parent.className().nestedClass("Companion")) {
                        this += KModifier.OVERRIDE
                        getter {
                            addStatement("return %T", parent.className())
                        }
                    }
                }
                is FileGenerating<*, *> -> {
                    property("parent", parent.fileMetadataClassName()) {
                        this += KModifier.OVERRIDE
                        getter {
                            addStatement("return %T", parent.fileMetadataClassName())
                        }
                    }
                }
            }

            property("descriptor", RuntimeTypes.FIELD_DESCRIPTOR_PROTO) {
                this += KModifier.OVERRIDE
                getter {
                    when (val parent = state.parent) {
                        is MessageGenerating<*, *> -> {
                            addStatement(
                                "return %T.descriptor.extension.first { it.number == ${state.descriptor.number} }",
                                parent.className()
                            )
                        }
                        is FileGenerating<*, *> -> {
                            addStatement(
                                "return %T.descriptor.extension.first { it.number == ${state.descriptor.number} }",
                                parent.fileMetadataClassName()
                            )
                        }
                    }
                }
            }

            property(
                "extendee",
                state.compiler().protoClassName(state.descriptor.extendee).nestedClass("Companion")
            ) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %T", state.compiler().protoClassName(state.descriptor.extendee))
                }
            }

            function("getProperty") {
                this += KModifier.OVERRIDE
                returns(KProperty::class.asClassName().parameterizedBy(STAR))
                addStatement(
                    "return %T::%M",
                    state.compiler().protoClassName(state.descriptor.extendee),
                    state.propertyMemberName()
                )
            }

            function("default") {
                this += KModifier.OVERRIDE
                returns(state.mutableFieldType())
                addStatement("return %L", state.defaultValue())
            }

            val message = state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE
            val enum = state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM
            val repeated = state.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
            val optional = state.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL
            val type = WireFormat.FieldType.values()[state.descriptor.type.ordinal]
            val packed = repeated && type.isPackable
            val method = type.name.toLowerCase()
            val any = state.descriptor.typeName == ".google.protobuf.Any"
            val mapEntry = state.mapEntry()

            function("write") {
                this += KModifier.OVERRIDE
                addParameter("writer", RuntimeTypes.WRITER)
                addParameter("value", fieldType)

                when {
                    packed && enum -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.number,
                                WireFormat.WIRETYPE_LENGTH_DELIMITED
                            )
                        }).beginLd().apply{ value.forEach { int32(it.number) } }.endLd()"
                    )
                    packed -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.number,
                                WireFormat.WIRETYPE_LENGTH_DELIMITED
                            )
                        }).beginLd().apply{ value.forEach { $method(it) } }.endLd()"
                    )
                    repeated && message -> {
                        if (mapEntry != null) {
                            val keyType =
                                WireFormat.FieldType.values()[mapEntry.fieldList.first { it.number == 1 }.type.ordinal]
                            val valueType =
                                WireFormat.FieldType.values()[mapEntry.fieldList.first { it.number == 2 }.type.ordinal]

                            addStatement(
                                "value.forEach { k, v -> writer.tag(${
                                    makeTag(
                                        state.descriptor.number,
                                        WireFormat.WIRETYPE_LENGTH_DELIMITED
                                    )
                                }).beginLd().tag(${
                                    makeTag(
                                        1,
                                        keyType.wireType
                                    )
                                }).${keyType.name.toLowerCase()}(k).tag(${
                                    makeTag(
                                        2,
                                        valueType.wireType
                                    )
                                }).${valueType.name.toLowerCase()}(v).endLd() }"
                            )
                        } else {
                            addStatement(
                                "value.forEach { writer.tag(${
                                    makeTag(
                                        state.descriptor.number,
                                        WireFormat.WIRETYPE_LENGTH_DELIMITED
                                    )
                                }).${if (any) "any" else "message"}(it) }"
                            )
                        }
                    }
                    repeated -> addStatement(
                        "value.forEach { writer.tag(${
                            makeTag(
                                state.descriptor.number,
                                type.wireType
                            )
                        }).$method(it) }"
                    )
                    enum -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.number,
                                type.wireType
                            )
                        }).int32(value.number)"
                    )
                    message -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.number,
                                WireFormat.WIRETYPE_LENGTH_DELIMITED
                            )
                        }).${if (any) "any" else "message"}(value)"
                    )
                    else -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.number,
                                type.wireType
                            )
                        }).$method(value)"
                    )
                }
            }

            function("read") {
                annotation(RuntimeTypes.INTERNAL_PROTO_API)
                this += KModifier.OVERRIDE
                addParameter("reader", RuntimeTypes.READER)
                addParameter("number", Int::class)
                addParameter("wire", Int::class)
                addParameter("extension", RuntimeTypes.MESSAGE_EXTENSION.parameterizedBy(fieldType).copy(true))
                returns(RuntimeTypes.MESSAGE_EXTENSION.parameterizedBy(fieldType))
                if (repeated) {
                    if (mapEntry != null) {
                        addStatement("val value = extension?.value ?: mutableMapOf()")
                    } else {
                        addStatement("val value = extension?.value ?: mutableListOf()")
                    }
                }

                when {
                    packed && enum -> addStatement(
                        "reader.packed(wire) { value·+=·%T(it.int32()) }",
                        state.compiler().protoClassName(state.descriptor.typeName)
                    )
                    packed -> addStatement("reader.packed(wire) { value·+=·it.${method}() }")
                    repeated && message -> {
                        if (mapEntry != null) {
                            val keyDescriptor = mapEntry.fieldList.first { it.number == 1 }
                            val keyType = WireFormat.FieldType.values()[keyDescriptor.type.ordinal]
                            val valueDescriptor = mapEntry.fieldList.first { it.number == 2 }
                            val valueType = WireFormat.FieldType.values()[valueDescriptor.type.ordinal]

                            when (valueType) {
                                WireFormat.FieldType.MESSAGE -> {
                                    addStatement(
                                        "reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { %T.newMutable().apply { readFrom(reader) } }) { k,·v·-> value[k]·=·v }",
                                        state.compiler().protoClassName(valueDescriptor.typeName)
                                    )
                                }
                                WireFormat.FieldType.ENUM -> {
                                    addStatement(
                                        "reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { %T(it.int32()) }) { k,·v·-> value[k]·=·v }",
                                        state.compiler().protoClassName(valueDescriptor.typeName)
                                    )
                                }
                                else -> {
                                    addStatement("reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { it.${valueType.name.toLowerCase()}() }) { k,·v·-> value[k]·=·v }")
                                }
                            }
                        } else if (any) {
                            addStatement("-> value += reader.any()")
                        } else {
                            addStatement(
                                "value += %T.newMutable().apply { readFrom(reader) }",
                                state.compiler().protoClassName(state.descriptor.typeName)
                            )
                        }
                    }
                    repeated -> addStatement("value += reader.${method}()")
                    message -> if (any) {
                        addStatement("return wrap(reader.any())")
                    } else {
                        addStatement(
                            "return wrap(%T.newMutable().apply { readFrom(reader) })",
                            state.compiler().protoClassName(state.descriptor.typeName)
                        )
                    }
                    enum -> addStatement(
                        "return wrap(%T(reader.int32()))",
                        state.compiler().protoClassName(state.descriptor.typeName)
                    )
                    else -> addStatement("return wrap(reader.${method}())")
                }

                if (repeated) {
                    addStatement("return wrap(value)")
                }
            }
        }

        when (val target = state.target) {
            is FileSpec.Builder -> target.addType(support)
            is TypeSpec.Builder -> target.addType(support)
        }

        return true
    }
}