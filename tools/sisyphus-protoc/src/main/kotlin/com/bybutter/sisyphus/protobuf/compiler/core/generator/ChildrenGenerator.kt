package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileParentGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageParentGeneratingState

class FileMessageChildGenerator : GroupedGenerator<FileParentGeneratingState> {
    override fun generate(state: FileParentGeneratingState): Boolean {
        for (message in state.descriptor.messages) {
            if (message.mapEntry()) continue
            state.target += message.className()
        }
        return true
    }
}

class FileEnumChildGenerator : GroupedGenerator<FileParentGeneratingState> {
    override fun generate(state: FileParentGeneratingState): Boolean {
        for (enum in state.descriptor.enums) {
            state.target += enum.className()
        }
        return true
    }
}

class FileExtensionChildGenerator : GroupedGenerator<FileParentGeneratingState> {
    override fun generate(state: FileParentGeneratingState): Boolean {
        for (extension in state.descriptor.extensions) {
            state.target += extension.supportClassName()
        }
        return true
    }
}

class NestedMessageChildGenerator : GroupedGenerator<MessageParentGeneratingState> {
    override fun generate(state: MessageParentGeneratingState): Boolean {
        for (message in state.descriptor.messages) {
            if (message.mapEntry()) continue
            state.target += message.className()
        }
        return true
    }
}

class NestedEnumChildGenerator : GroupedGenerator<MessageParentGeneratingState> {
    override fun generate(state: MessageParentGeneratingState): Boolean {
        for (enum in state.descriptor.enums) {
            state.target += enum.className()
        }
        return true
    }
}

class NestedExtensionChildGenerator : GroupedGenerator<MessageParentGeneratingState> {
    override fun generate(state: MessageParentGeneratingState): Boolean {
        for (extension in state.descriptor.extensions) {
            state.target += extension.supportClassName()
        }
        return true
    }
}
