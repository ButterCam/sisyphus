package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.companion
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.InternalFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageCompanionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageInterfaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageParentRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MutableMessageInterfaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.implements
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kInterface
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.buildCodeBlock

class MessageApiGenerator : GroupedGenerator<ApiFileGeneratingState> {
    override fun generate(state: ApiFileGeneratingState): Boolean {
        for (message in state.descriptor.messages) {
            if(message.mapEntry()) continue
            state.target.addType(kInterface(message.name()) {
                MessageInterfaceGeneratingState(state, message, this).advance()
            })
        }

        return true
    }
}

class MessageInterfaceBasicGenerator : GroupedGenerator<MessageInterfaceGeneratingState> {
    override fun generate(state: MessageInterfaceGeneratingState): Boolean {
        state.target.apply {
            this implements RuntimeTypes.MESSAGE.parameterizedBy(
                state.descriptor.className(),
                state.descriptor.mutableClassName()
            )
            addKdoc(state.descriptor.document())

            for (message in state.descriptor.messages) {
                if(message.mapEntry()) continue
                state.target.addType(kInterface(message.name()) {
                    MessageInterfaceGeneratingState(state, message, this).advance()
                })
            }

            companion {
                this extends state.descriptor.supportClassName()

                MessageCompanionGeneratingState(state, state.descriptor, this).advance()
            }
        }
        return true
    }
}

class MessageInternalGenerator : GroupedGenerator<InternalFileGeneratingState> {
    override fun generate(state: InternalFileGeneratingState): Boolean {
        for (message in state.descriptor.messages) {
            if(message.mapEntry()) continue

            state.target.addType(kInterface(message.mutableName()) {
                MutableMessageInterfaceGeneratingState(state, message, this).advance()
            })

            state.target.addType(kClass(message.implementationName()) {
                MessageImplementationGeneratingState(state, message, this).advance()
            })

            state.target.addType(kClass(message.supportName()) {
                MessageSupportGeneratingState(state, message, this).advance()
            })
        }
        return true
    }
}

class MutableMessageInterfaceBasicGenerator :
    GroupedGenerator<MutableMessageInterfaceGeneratingState> {
    override fun generate(state: MutableMessageInterfaceGeneratingState): Boolean {
        state.target.apply {
            this implements RuntimeTypes.MUTABLE_MESSAGE.parameterizedBy(
                state.descriptor.className(),
                state.descriptor.mutableClassName()
            )
            this implements state.descriptor.className()

            for (message in state.descriptor.messages) {
                if(message.mapEntry()) continue
                state.target.addType(kInterface(message.mutableName()) {
                    MutableMessageInterfaceGeneratingState(state, message, this).advance()
                })
            }
        }
        return true
    }
}

class MessageImplementationBasicGenerator :
    GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            this += KModifier.INTERNAL
            annotation(RuntimeTypes.INTERNAL_PROTO_API)
            this extends RuntimeTypes.ABSTRACT_MUTABLE_MESSAGE.parameterizedBy(
                state.descriptor.className(),
                state.descriptor.mutableClassName()
            )
            this implements state.descriptor.mutableClassName()

            for (message in state.descriptor.messages) {
                if(message.mapEntry()) continue
                state.target.addType(kClass(message.implementationName()) {
                    MessageImplementationGeneratingState(state, message, this).advance()
                })
            }
        }
        return true
    }
}

class MessageSupportBasicGenerator : GroupedGenerator<MessageSupportGeneratingState> {
    override fun generate(state: MessageSupportGeneratingState): Boolean {
        state.target.apply {
            this += KModifier.OPEN
            this extends RuntimeTypes.MESSAGE_SUPPORT.parameterizedBy(
                state.descriptor.className(),
                state.descriptor.mutableClassName()
            )

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
                    when (val parent = state.descriptor.parent) {
                        is FileDescriptor -> {
                            addStatement(
                                "%T.descriptor.messageType.first{ it.name == %S }",
                                parent.fileMetadataClassName(),
                                state.descriptor.descriptor.name
                            )
                        }
                        is MessageDescriptor -> {
                            addStatement(
                                "%T.descriptor.nestedType.first{ it.name == %S }",
                                parent.className(),
                                state.descriptor.descriptor.name
                            )
                        }
                    }
                    endControlFlow()
                })
            }

            function("newMutable") {
                this += KModifier.OVERRIDE
                annotation(RuntimeTypes.INTERNAL_PROTO_API)
                returns(state.descriptor.mutableClassName())
                addStatement("return %T()", state.descriptor.implementationClassName())
            }

            function("register") {
                this += KModifier.OVERRIDE
                MessageParentRegisterGeneratingState(state, state.descriptor, this).advance()
            }

            for (message in state.descriptor.messages) {
                if(message.mapEntry()) continue
                state.target.addType(kClass(message.supportName()) {
                    MessageSupportGeneratingState(state, message, this).advance()
                })
            }
            return true
        }
    }
}
