package com.bybutter.sisyphus.protobuf.compiler.generator.resourcename

import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageOptionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generator.CodeGenerator
import com.bybutter.sisyphus.protobuf.compiler.kInterface
import com.google.api.ResourceProto

class ResourceNameGenerator: CodeGenerator<MessageOptionGeneratingState> {
    override fun generate(state: MessageOptionGeneratingState): Boolean {
        val resource = state.descriptor.options.getExtension(ResourceProto.resource)

        state.target.apply {
            addType(kInterface("Name") {

            })
        }

        return false
    }
}