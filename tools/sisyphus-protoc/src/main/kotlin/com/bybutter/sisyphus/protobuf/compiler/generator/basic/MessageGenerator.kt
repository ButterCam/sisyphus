package com.bybutter.sisyphus.protobuf.compiler.generator.basic

import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.companion
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.generating.FileGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.advance
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ApiGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.EnumOptionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.FieldGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.FieldImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ImplementationGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageOptionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageOptionSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MutableFieldGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MutableGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MutableOneofGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedEnumGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedEnumRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedEnumSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedExtensionFieldGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedExtensionFieldSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedExtensionRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedMessageGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedMessageImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedMessageRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedMessageSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.NestedMutableMessageGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.OneofGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.OneofImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.SupportGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.className
import com.bybutter.sisyphus.protobuf.compiler.generating.document
import com.bybutter.sisyphus.protobuf.compiler.generating.fileMetadataClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.fullProtoName
import com.bybutter.sisyphus.protobuf.compiler.generating.implementationClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.implementationName
import com.bybutter.sisyphus.protobuf.compiler.generating.mutableClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.mutableName
import com.bybutter.sisyphus.protobuf.compiler.generating.name
import com.bybutter.sisyphus.protobuf.compiler.generating.supportClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.supportName
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.implements
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kInterface
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock

open class MessageApiGenerator : UniqueGenerator<MessageGenerating<*, *>> {
    override fun generate(state: MessageGenerating<*, *>): Boolean {
        if (state.descriptor.options.mapEntry) return false
        if (state !is ApiGenerating) return false

        val messageType = kInterface(state.name()) {
            this implements RuntimeTypes.MESSAGE.parameterizedBy(state.className(), state.mutableClassName())
            addKdoc(state.document())

            for (fieldDescriptor in state.descriptor.fieldList) {
                FieldGeneratingState(state, fieldDescriptor, this).advance()
            }

            for (oneofDescriptorProto in state.descriptor.oneofDeclList) {
                OneofGeneratingState(state, oneofDescriptorProto, this).advance()
            }

            for (descriptorProto in state.descriptor.nestedTypeList) {
                NestedMessageGeneratingState(state, descriptorProto, this).advance()
            }

            for (enum in state.descriptor.enumTypeList) {
                NestedEnumGeneratingState(state, enum, this).advance()
            }

            MessageOptionGeneratingState(state, state.descriptor, this).advance()

            companion {
                this extends state.supportClassName()

                for (extension in state.descriptor.extensionList) {
                    NestedExtensionFieldGeneratingState(state, extension, this).advance()
                }
            }
        }

        when (val target = state.target) {
            is FileSpec.Builder -> target.addType(messageType)
            is TypeSpec.Builder -> target.addType(messageType)
        }

        return true
    }
}

open class MutableMessageGenerator : UniqueGenerator<MessageGenerating<*, *>> {
    override fun generate(state: MessageGenerating<*, *>): Boolean {
        if (state.descriptor.options.mapEntry) return false
        if (state !is MutableGenerating) return false

        val messageType = kInterface(state.mutableName()) {
            this implements RuntimeTypes.MUTABLE_MESSAGE.parameterizedBy(state.className(), state.mutableClassName())
            this implements state.className()

            for (fieldDescriptor in state.descriptor.fieldList) {
                MutableFieldGeneratingState(state, fieldDescriptor, this).advance()
            }

            for (oneofDescriptorProto in state.descriptor.oneofDeclList) {
                MutableOneofGeneratingState(state, oneofDescriptorProto, this).advance()
            }

            for (descriptor in state.descriptor.nestedTypeList) {
                NestedMutableMessageGeneratingState(state, descriptor, this).advance()
            }
        }

        when (val target = state.target) {
            is FileSpec.Builder -> target.addType(messageType)
            is TypeSpec.Builder -> target.addType(messageType)
        }

        return true
    }
}

open class MessageImplementationGenerator : UniqueGenerator<MessageGenerating<*, *>> {
    override fun generate(state: MessageGenerating<*, *>): Boolean {
        if (state.descriptor.options.mapEntry) return false
        if (state !is ImplementationGenerating) return false

        val messageType = kClass(state.implementationName()) {
            this += KModifier.INTERNAL
            annotation(RuntimeTypes.INTERNAL_PROTO_API)
            this extends RuntimeTypes.ABSTRACT_MUTABLE_MESSAGE.parameterizedBy(
                state.className(),
                state.mutableClassName()
            )
            this implements state.mutableClassName()

            for (fieldDescriptor in state.descriptor.fieldList) {
                FieldImplementationGeneratingState(state, fieldDescriptor, this).advance()
            }

            for (oneofDescriptorProto in state.descriptor.oneofDeclList) {
                OneofImplementationGeneratingState(state, oneofDescriptorProto, this).advance()
            }

            MessageFunctionGeneratingState(state, state.descriptor, this).advance()

            for (descriptor in state.descriptor.nestedTypeList) {
                if (descriptor.options?.mapEntry != true) {
                    NestedMessageImplementationGeneratingState(state, descriptor, this).advance()
                }
            }
        }

        when (val target = state.target) {
            is FileSpec.Builder -> target.addType(messageType)
            is TypeSpec.Builder -> target.addType(messageType)
        }

        return true
    }
}

open class MessageSupportGenerator : UniqueGenerator<MessageGenerating<*, *>> {
    override fun generate(state: MessageGenerating<*, *>): Boolean {
        if (state.descriptor.options.mapEntry) return false
        if (state !is SupportGenerating) return false

        val messageType = kClass(state.supportName()) {
            this += KModifier.OPEN
            this extends RuntimeTypes.MESSAGE_SUPPORT.parameterizedBy(state.className(), state.mutableClassName())

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

            property("descriptor", RuntimeTypes.DESCRIPTOR_PROTO) {
                this += KModifier.OVERRIDE
                delegate(buildCodeBlock {
                    beginControlFlow("%M", MemberName("kotlin", "lazy"))
                    when (val parent = state.parent) {
                        is FileGenerating<*, *> -> {
                            addStatement(
                                "%T.descriptor.messageType.first{ it.name == %S }",
                                parent.fileMetadataClassName(),
                                state.descriptor.name
                            )
                        }
                        is MessageGenerating<*, *> -> {
                            addStatement(
                                "%T.descriptor.nestedType.first{ it.name == %S }",
                                parent.className(),
                                state.descriptor.name
                            )
                        }
                    }
                    endControlFlow()
                })
            }

            function("newMutable") {
                this += KModifier.OVERRIDE
                annotation(RuntimeTypes.INTERNAL_PROTO_API)
                returns(state.mutableClassName())
                addStatement("return %T()", state.implementationClassName())
            }

            function("register") {
                this += KModifier.OVERRIDE

                for (message in state.descriptor.nestedTypeList) {
                    NestedMessageRegisterGeneratingState(state, message, this).advance()
                }

                for (enum in state.descriptor.enumTypeList) {
                    NestedEnumRegisterGeneratingState(state, enum, this).advance()
                }

                for (extension in state.descriptor.extensionList) {
                    NestedExtensionRegisterGeneratingState(state, extension, this).advance()
                }
            }

            for (enumDescriptor in state.descriptor.enumTypeList) {
                NestedEnumSupportGeneratingState(state, enumDescriptor, this).advance()
            }

            for (descriptor in state.descriptor.nestedTypeList) {
                NestedMessageSupportGeneratingState(state, descriptor, this).advance()
            }

            for (extension in state.descriptor.extensionList) {
                NestedExtensionFieldSupportGeneratingState(state, extension, this).advance()
            }

            MessageOptionSupportGeneratingState(state, state.descriptor, this).advance()
        }

        when (val target = state.target) {
            is FileSpec.Builder -> target.addType(messageType)
            is TypeSpec.Builder -> target.addType(messageType)
        }

        return true
    }
}