package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.booster.ProtobufBoosterGeneratingState

class FileBoosterGenerator : GroupedGenerator<ProtobufBoosterGeneratingState> {
    override fun generate(state: ProtobufBoosterGeneratingState): Boolean {
        state.target.builder.addStatement(
            "%T.register(%T)",
            RuntimeTypes.PROTO_TYPES,
            state.descriptor.fileMetadataClassName()
        )
        return true
    }
}
