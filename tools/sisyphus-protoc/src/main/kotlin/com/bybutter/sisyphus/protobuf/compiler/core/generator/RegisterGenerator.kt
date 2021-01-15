package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.core.state.EnumRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.ExtensionRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileParentRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageParentRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance

class MessageParentRegisterGenerator :
    GroupedGenerator<FileParentRegisterGeneratingState> {
    override fun generate(state: FileParentRegisterGeneratingState): Boolean {
        for (message in state.descriptor.messages) {
            MessageRegisterGeneratingState(state, message, state.target).advance()
        }
        return true
    }
}

class NestMessageParentRegisterGenerator :
    GroupedGenerator<MessageParentRegisterGeneratingState> {
    override fun generate(state: MessageParentRegisterGeneratingState): Boolean {
        for (message in state.descriptor.messages) {
            MessageRegisterGeneratingState(state, message, state.target).advance()
        }
        return true
    }
}

class EnumParentRegisterGenerator :
    GroupedGenerator<FileParentRegisterGeneratingState> {
    override fun generate(state: FileParentRegisterGeneratingState): Boolean {
        for (enum in state.descriptor.enums) {
            EnumRegisterGeneratingState(state, enum, state.target).advance()
        }
        return true
    }
}

class NestEnumParentRegisterGenerator :
    GroupedGenerator<MessageParentRegisterGeneratingState> {
    override fun generate(state: MessageParentRegisterGeneratingState): Boolean {
        for (enum in state.descriptor.enums) {
            EnumRegisterGeneratingState(state, enum, state.target).advance()
        }
        return true
    }
}

class ExtensionParentRegisterGenerator :
    GroupedGenerator<FileParentRegisterGeneratingState> {
    override fun generate(state: FileParentRegisterGeneratingState): Boolean {
        for (extension in state.descriptor.extensions) {
            ExtensionRegisterGeneratingState(state, extension, state.target).advance()
        }
        return true
    }
}

class NestExtensionParentRegisterGenerator :
    GroupedGenerator<MessageParentRegisterGeneratingState> {
    override fun generate(state: MessageParentRegisterGeneratingState): Boolean {
        for (extension in state.descriptor.extensions) {
            ExtensionRegisterGeneratingState(state, extension, state.target).advance()
        }
        return true
    }
}

class MessageRegisterGenerator :
    GroupedGenerator<MessageRegisterGeneratingState> {
    override fun generate(state: MessageRegisterGeneratingState): Boolean {
        if (state.descriptor.mapEntry()) return false
        state.target.addStatement("%T.register(%T)", RuntimeTypes.PROTO_TYPES, state.descriptor.className())
        return true
    }
}

class EnumRegisterGenerator : GroupedGenerator<EnumRegisterGeneratingState> {
    override fun generate(state: EnumRegisterGeneratingState): Boolean {
        state.target.addStatement("%T.register(%T)", RuntimeTypes.PROTO_TYPES, state.descriptor.className())
        return true
    }
}

class ExtensionRegisterGenerator :
    GroupedGenerator<ExtensionRegisterGeneratingState> {
    override fun generate(state: ExtensionRegisterGeneratingState): Boolean {
        state.target.addStatement("%T.register(%T)", RuntimeTypes.PROTO_TYPES, state.descriptor.supportClassName())
        return true
    }
}
