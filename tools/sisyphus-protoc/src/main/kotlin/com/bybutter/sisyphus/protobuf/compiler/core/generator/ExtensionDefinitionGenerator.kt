package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.clearFunction
import com.bybutter.sisyphus.protobuf.compiler.core.state.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.ExtensionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.ExtensionSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.InternalFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageCompanionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.defaultValue
import com.bybutter.sisyphus.protobuf.compiler.elementType
import com.bybutter.sisyphus.protobuf.compiler.enumType
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.fieldType
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.hasFunction
import com.bybutter.sisyphus.protobuf.compiler.kFun
import com.bybutter.sisyphus.protobuf.compiler.kObject
import com.bybutter.sisyphus.protobuf.compiler.kProperty
import com.bybutter.sisyphus.protobuf.compiler.mapEntry
import com.bybutter.sisyphus.protobuf.compiler.mutableFieldType
import com.bybutter.sisyphus.protobuf.compiler.name
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
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KProperty

class ExtensionApiGenerator : GroupedGenerator<ApiFileGeneratingState> {
    override fun generate(state: ApiFileGeneratingState): Boolean {
        for (extension in state.descriptor.extensions) {
            ExtensionGeneratingState(state, extension, state.target).advance()
        }
        return true
    }
}

class NestedExtensionApiGenerator :
    GroupedGenerator<MessageCompanionGeneratingState> {
    override fun generate(state: MessageCompanionGeneratingState): Boolean {
        for (extension in state.descriptor.extensions) {
            ExtensionGeneratingState(state, extension, state.target).advance()
        }
        return true
    }
}

class ExtensionSupportGenerator :
    GroupedGenerator<InternalFileGeneratingState> {
    override fun generate(state: InternalFileGeneratingState): Boolean {
        for (extension in state.descriptor.extensions) {
            state.target.addType(kObject(extension.supportName()) {
                ExtensionSupportGeneratingState(state, extension, this).advance()
            })
        }
        return true
    }
}

class NestedExtensionSupportGenerator :
    GroupedGenerator<MessageSupportGeneratingState> {
    override fun generate(state: MessageSupportGeneratingState): Boolean {
        for (extension in state.descriptor.extensions) {
            state.target.addType(kObject(extension.supportName()) {
                ExtensionSupportGeneratingState(state, extension, this).advance()
            })
        }
        return true
    }
}

class ExtensionDefinitionGenerator :
    GroupedGenerator<ExtensionGeneratingState> {
    override fun generate(state: ExtensionGeneratingState): Boolean {
        val property = kProperty(state.descriptor.descriptor.jsonName, state.descriptor.fieldType()) {
            receiver(state.descriptor.extendee().className())
            addKdoc(state.descriptor.document())
            if (state.descriptor.descriptor.options?.deprecated == true) {
                annotation(Deprecated::class.asClassName()) {
                    addMember("message = %S", "${state.descriptor.name()} has been marked as deprecated")
                }
            }
            getter {
                addStatement("return this[${state.descriptor.descriptor.number}]")
            }
        }

        val hasFunc = kFun(state.descriptor.hasFunction()) {
            receiver(state.descriptor.extendee().className())
            returns(Boolean::class)
            addStatement("return this.has(${state.descriptor.descriptor.number})")
        }

        val mutableProperty = kProperty(state.descriptor.descriptor.jsonName, state.descriptor.fieldType()) {
            receiver(state.descriptor.extendee().mutableClassName())
            mutable(true)
            addKdoc(state.descriptor.document())
            if (state.descriptor.descriptor.options?.deprecated == true) {
                annotation(Deprecated::class.asClassName()) {
                    addMember("message = %S", "${state.descriptor.name()} has been marked as deprecated")
                }
            }
            getter {
                addStatement("return this[${state.descriptor.descriptor.number}]")
            }
            setter {
                addParameter("value", state.descriptor.fieldType())
                addStatement("this[${state.descriptor.descriptor.number}] = value")
            }
        }

        val clearFunc = kFun(state.descriptor.clearFunction()) {
            receiver(state.descriptor.extendee().mutableClassName())
            returns(state.descriptor.fieldType().copy(true))
            addStatement(
                "return this.clear(${state.descriptor.descriptor.number}) as %T",
                state.descriptor.fieldType().copy(true)
            )
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

class ExtensionSupportBasicGenerator :
    GroupedGenerator<ExtensionSupportGeneratingState> {
    override fun generate(state: ExtensionSupportGeneratingState): Boolean {
        state.target.apply {
            val fieldType = state.descriptor.mutableFieldType().copy(false)
            this extends RuntimeTypes.EXTENSION_SUPPORT.parameterizedBy(fieldType)

            property("name", String::class) {
                this += KModifier.OVERRIDE
                getter {
                    when (val parent = state.descriptor.parent) {
                        is MessageDescriptor -> {
                            addStatement(
                                "return %S",
                                "${
                                    state.descriptor.extendee().fullProtoName()
                                }.${state.descriptor.descriptor.name}@${parent.fullProtoName()}"
                            )
                        }
                        is FileDescriptor -> {
                            addStatement(
                                "return %S",
                                "${
                                    state.descriptor.extendee().fullProtoName()
                                }.${state.descriptor.descriptor.name}@${parent.descriptor.`package`}"
                            )
                        }
                    }
                }
            }

            when (val parent = state.descriptor.parent) {
                is MessageDescriptor -> {
                    property("parent", parent.className().nestedClass("Companion")) {
                        this += KModifier.OVERRIDE
                        getter {
                            addStatement("return %T", parent.className())
                        }
                    }
                }
                is FileDescriptor -> {
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
                    when (val parent = state.descriptor.parent) {
                        is MessageDescriptor -> {
                            addStatement(
                                "return %T.descriptor.extension.first { it.number == ${state.descriptor.descriptor.number} }",
                                parent.className()
                            )
                        }
                        is FileDescriptor -> {
                            addStatement(
                                "return %T.descriptor.extension.first { it.number == ${state.descriptor.descriptor.number} }",
                                parent.fileMetadataClassName()
                            )
                        }
                    }
                }
            }

            property(
                "extendee",
                state.descriptor.extendee().className().nestedClass("Companion")
            ) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %T", state.descriptor.extendee().className())
                }
            }

            function("getProperty") {
                this += KModifier.OVERRIDE
                returns(KProperty::class.asClassName().parameterizedBy(STAR))
                addStatement(
                    "return %T::%M",
                    state.descriptor.extendee().className(),
                    state.descriptor.propertyMemberName()
                )
            }

            function("default") {
                this += KModifier.OVERRIDE
                returns(state.descriptor.mutableFieldType())
                addStatement("return %L", state.descriptor.defaultValue())
            }

            val message = state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE
            val enum = state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM
            val repeated =
                state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
            val optional =
                state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL
            val type = WireFormat.FieldType.values()[state.descriptor.descriptor.type.ordinal]
            val packed = repeated && type.isPackable
            val method = type.name.toLowerCase()
            val any = state.descriptor.descriptor.typeName == ".google.protobuf.Any"
            val mapEntry = state.descriptor.mapEntry()

            function("write") {
                this += KModifier.OVERRIDE
                addParameter("writer", RuntimeTypes.WRITER)
                addParameter("value", fieldType)

                when {
                    packed && enum -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.descriptor.number,
                                WireFormat.WIRETYPE_LENGTH_DELIMITED
                            )
                        }).beginLd().apply{ value.forEach { int32(it.number) } }.endLd()"
                    )
                    packed -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.descriptor.number,
                                WireFormat.WIRETYPE_LENGTH_DELIMITED
                            )
                        }).beginLd().apply{ value.forEach { $method(it) } }.endLd()"
                    )
                    repeated && message -> {
                        if (mapEntry != null) {
                            val keyType =
                                WireFormat.FieldType.values()[mapEntry.fields.first { it.descriptor.number == 1 }.descriptor.type.ordinal]
                            val valueType =
                                WireFormat.FieldType.values()[mapEntry.fields.first { it.descriptor.number == 2 }.descriptor.type.ordinal]

                            addStatement(
                                "value.forEach { k, v -> writer.tag(${
                                    makeTag(
                                        state.descriptor.descriptor.number,
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
                                        state.descriptor.descriptor.number,
                                        WireFormat.WIRETYPE_LENGTH_DELIMITED
                                    )
                                }).${if (any) "any" else "message"}(it) }"
                            )
                        }
                    }
                    repeated -> addStatement(
                        "value.forEach { writer.tag(${
                            makeTag(
                                state.descriptor.descriptor.number,
                                type.wireType
                            )
                        }).$method(it) }"
                    )
                    enum -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.descriptor.number,
                                type.wireType
                            )
                        }).int32(value.number)"
                    )
                    message -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.descriptor.number,
                                WireFormat.WIRETYPE_LENGTH_DELIMITED
                            )
                        }).${if (any) "any" else "message"}(value)"
                    )
                    else -> addStatement(
                        "writer.tag(${
                            makeTag(
                                state.descriptor.descriptor.number,
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
                        state.descriptor.enumType()!!.className()
                    )
                    packed -> addStatement("reader.packed(wire) { value·+=·it.$method() }")
                    repeated && message -> {
                        if (mapEntry != null) {
                            val keyDescriptor = mapEntry.fields.first { it.descriptor.number == 1 }
                            val keyType = WireFormat.FieldType.values()[keyDescriptor.descriptor.type.ordinal]
                            val valueDescriptor = mapEntry.fields.first { it.descriptor.number == 2 }
                            val valueType = WireFormat.FieldType.values()[valueDescriptor.descriptor.type.ordinal]

                            when (valueType) {
                                WireFormat.FieldType.MESSAGE -> {
                                    addStatement(
                                        "reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { %T.newMutable().apply { readFrom(reader) } }) { k,·v·-> value[k]·=·v }",
                                        valueDescriptor.elementType()
                                    )
                                }
                                WireFormat.FieldType.ENUM -> {
                                    addStatement(
                                        "reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { %T(it.int32()) }) { k,·v·-> value[k]·=·v }",
                                        valueDescriptor.elementType()
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
                                state.descriptor.elementType()
                            )
                        }
                    }
                    repeated -> addStatement("value += reader.$method()")
                    message -> if (any) {
                        addStatement("return wrap(reader.any())")
                    } else {
                        addStatement(
                            "return wrap(%T.newMutable().apply { readFrom(reader) })",
                            state.descriptor.elementType()
                        )
                    }
                    enum -> addStatement(
                        "return wrap(%T(reader.int32()))",
                        state.descriptor.elementType()
                    )
                    else -> addStatement("return wrap(reader.$method())")
                }

                if (repeated) {
                    addStatement("return wrap(value)")
                }
            }
        }
        return true
    }
}
