package com.bybutter.sisyphus.protobuf.compiler.generator.basic

import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.companion
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.generating.EnumGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.FileGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.advance
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ApiGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.EnumOptionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.EnumOptionSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.EnumValueGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.SupportGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.className
import com.bybutter.sisyphus.protobuf.compiler.generating.fileMetadataClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.fullProtoName
import com.bybutter.sisyphus.protobuf.compiler.generating.name
import com.bybutter.sisyphus.protobuf.compiler.generating.supportClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.supportName
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.implements
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kEnum
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

open class EnumGenerator : UniqueGenerator<EnumGenerating<*, *>> {
    override fun generate(state: EnumGenerating<*, *>): Boolean {
        if (state !is ApiGenerating) return false

        val enum = kEnum(state.name()) {
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

            for (enumValue in state.descriptor.valueList) {
                EnumValueGeneratingState(state, enumValue, this).advance()
            }

            EnumOptionGeneratingState(state, state.descriptor, this).advance()

            companion {
                this extends state.supportClassName()
            }
        }

        when (val target = state.target) {
            is FileSpec.Builder -> target.addType(enum)
            is TypeSpec.Builder -> target.addType(enum)
        }
        return true
    }
}

open class EnumSupportGenerator : UniqueGenerator<EnumGenerating<*, *>> {
    override fun generate(state: EnumGenerating<*, *>): Boolean {
        if (state !is SupportGenerating) return false

        val type = kClass(state.supportName()) {
            this += KModifier.ABSTRACT
            this extends RuntimeTypes.ENUM_SUPPORT.parameterizedBy(state.className())
            addSuperclassConstructorParameter("%T::class", state.className())
            constructor {
                this += KModifier.INTERNAL
            }

            property("name", String::class) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %S", state.fullProtoName())
                }
            }

            when (val parent = state.parent) {
                is FileGenerating<*, *> -> {
                    property("parent", RuntimeTypes.FILE_SUPPORT) {
                        this += KModifier.OVERRIDE
                        getter {
                            addStatement("return %T", parent.fileMetadataClassName())
                        }
                    }
                }
                is MessageGenerating<*, *> -> {
                    property("parent", parent.className().nestedClass("Companion")) {
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
                    when (val parent = state.parent) {
                        is FileGenerating<*, *> -> {
                            addStatement(
                                "%T.descriptor.enumType.first{ it.name == %S }",
                                parent.fileMetadataClassName(),
                                state.descriptor.name
                            )
                        }
                        is MessageGenerating<*, *> -> {
                            addStatement(
                                "%T.descriptor.enumType.first{ it.name == %S }",
                                parent.className(),
                                state.descriptor.name
                            )
                        }
                    }
                    endControlFlow()
                })
            }

            EnumOptionSupportGeneratingState(state, state.descriptor, this).advance()
        }

        when (val target = state.target) {
            is FileSpec.Builder -> target.addType(type)
            is TypeSpec.Builder -> target.addType(type)
        }

        return true
    }
}

open class EnumValueGenerator : UniqueGenerator<EnumValueGeneratingState> {
    override fun generate(state: EnumValueGeneratingState): Boolean {
        state.target.addEnumConstant(state.name(), TypeSpec.anonymousClassBuilder()
            .addSuperclassConstructorParameter("%L", state.descriptor.number)
            .addSuperclassConstructorParameter("%S", state.descriptor.name)
            .build())
        return true
    }
}