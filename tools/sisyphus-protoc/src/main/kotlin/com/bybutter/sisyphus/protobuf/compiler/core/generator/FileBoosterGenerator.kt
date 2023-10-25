package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.booster.ProtobufBoosterGeneratingState

class FileBoosterGenerator : GroupedGenerator<ProtobufBoosterGeneratingState> {
    override fun generate(state: ProtobufBoosterGeneratingState): Boolean {
        state.target.builder.addStatement(
            "reflection.register(%T)",
            state.descriptor.fileMetadataClassName(),
        )
        return true
    }
}
