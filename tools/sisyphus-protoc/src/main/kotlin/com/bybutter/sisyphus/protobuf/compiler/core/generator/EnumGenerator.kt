package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.companion
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.EnumGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.EnumSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.EnumValueGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.InternalFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageInterfaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.implements
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kEnum
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

class EnumApiGenerator : com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ApiFileGeneratingState> {
    override fun generate(state: ApiFileGeneratingState): Boolean {
        for (enum in state.descriptor.enums) {
            state.target.addType(kEnum(enum.name()) {
                EnumGeneratingState(state, enum, this).advance()
            })
        }

        return true
    }
}

class NestedEnumGenerator : com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<MessageInterfaceGeneratingState> {
    override fun generate(state: MessageInterfaceGeneratingState): Boolean {
        for (enum in state.descriptor.enums) {
            state.target.addType(kEnum(enum.name()) {
                EnumGeneratingState(state, enum, this).advance()
            })
        }

        return true
    }
}

class EnumBasicGenerator : com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<EnumGeneratingState> {
    override fun generate(state: EnumGeneratingState): Boolean {
        state.target.apply {
            this implements RuntimeTypes.PROTO_ENUM

            constructor {
                addParameter("number", Int::class)
                addParameter("proto", String::class)
            }
            property("number", Int::class.asTypeName()) {
                this += KModifier.OVERRIDE
                initializer("number")
            }
            property("proto", String::class.asTypeName()) {
                this += KModifier.OVERRIDE
                initializer("proto")
            }

            companion {
                this extends state.descriptor.supportClassName()
            }

            for (value in state.descriptor.values) {
                EnumValueGeneratingState(state, value, this).advance()
            }
        }
        return true
    }
}

class EnumSupportGenerator : com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<InternalFileGeneratingState> {
    override fun generate(state: InternalFileGeneratingState): Boolean {
        for (enum in state.descriptor.enums) {
            state.target.addType(kClass(enum.supportName()) {
                EnumSupportGeneratingState(state, enum, this).advance()
            })
        }
        return true
    }
}

class NestedEnumSupportGenerator :
    com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<MessageSupportGeneratingState> {
    override fun generate(state: MessageSupportGeneratingState): Boolean {
        for (enum in state.descriptor.enums) {
            state.target.addType(kClass(enum.supportName()) {
                EnumSupportGeneratingState(state, enum, this).advance()
            })
        }
        return true
    }
}

class EnumSupportBasicGenerator : com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<EnumSupportGeneratingState> {
    override fun generate(state: EnumSupportGeneratingState): Boolean {
        state.target.apply {
            this += KModifier.ABSTRACT
            this extends RuntimeTypes.ENUM_SUPPORT.parameterizedBy(state.descriptor.className())
            addSuperclassConstructorParameter("%T::class", state.descriptor.className())
            constructor {
                this += KModifier.INTERNAL
            }

            property("name", String::class) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %S", state.descriptor.fullProtoName())
                }
            }

            when (val parent = state.descriptor.parent) {
                is FileDescriptor -> {
                    property("parent", RuntimeTypes.FILE_SUPPORT) {
                        this += KModifier.OVERRIDE
                        getter {
                            addStatement("return %T", parent.fileMetadataClassName())
                        }
                    }
                }
                is MessageDescriptor -> {
                    property("parent", parent.className()) {
                        this += KModifier.OVERRIDE
                        getter {
                            addStatement("return %T", parent.className())
                        }
                    }
                }
            }

            property("descriptor", RuntimeTypes.ENUM_DESCRIPTOR_PROTO) {
                this += KModifier.OVERRIDE
                delegate(buildCodeBlock {
                    beginControlFlow("%M", MemberName("kotlin", "lazy"))
                    when (val parent = state.descriptor.parent) {
                        is FileDescriptor -> {
                            addStatement(
                                "%T.descriptor.enumType.first{ it.name == %S }",
                                parent.fileMetadataClassName(),
                                state.descriptor.descriptor.name
                            )
                        }
                        is MessageDescriptor -> {
                            addStatement(
                                "%T.descriptor.enumType.first{ it.name == %S }",
                                parent.className(),
                                state.descriptor.descriptor.name
                            )
                        }
                    }
                    endControlFlow()
                })
            }
        }
        return true
    }
}
