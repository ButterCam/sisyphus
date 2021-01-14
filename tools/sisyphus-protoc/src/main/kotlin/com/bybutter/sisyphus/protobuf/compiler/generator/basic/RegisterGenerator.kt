package com.bybutter.sisyphus.protobuf.compiler.generator.basic

import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.generating.EnumGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.ExtensionFieldGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.RegisterGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.className
import com.bybutter.sisyphus.protobuf.compiler.generating.supportClassName
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator

open class RegisterGenerator : UniqueGenerator<RegisterGenerating<*, *>> {
    override fun generate(state: RegisterGenerating<*, *>): Boolean {
        when (state) {
            is MessageGenerating<*, *> -> {
                if (state.descriptor.options.mapEntry) return false
                state.target.addStatement("%T.register(%T)", RuntimeTypes.PROTO_TYPES, state.className())
            }
            is EnumGenerating<*, *> -> {
                state.target.addStatement("%T.register(%T)", RuntimeTypes.PROTO_TYPES, state.className())
            }
            is ExtensionFieldGenerating<*, *> -> {
                state.target.addStatement("%T.register(%T)", RuntimeTypes.PROTO_TYPES, state.supportClassName())
            }
            else -> return false
        }
        return true
    }
}