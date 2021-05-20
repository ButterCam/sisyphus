package com.bybutter.sisyphus.protobuf.compiler.booster

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance

class ProtobufBoosterGenerator : GroupedGenerator<FileGeneratingState> {
    override fun generate(state: FileGeneratingState): Boolean {
        ProtobufBoosterGeneratingState(state, state.descriptor, state.compiler.boosterContext()).advance()
        return true
    }
}
