package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.DescriptorNode

interface GeneratingState<TDesc : DescriptorNode<*>, TTarget> {
    val descriptor: TDesc

    val target: TTarget
}

interface ChildGeneratingState<TDesc : DescriptorNode<*>, TTarget> : GeneratingState<TDesc, TTarget> {
    val parent: GeneratingState<*, *>
}

fun GeneratingState<*, *>.advance() {
    var state = this

    while (true) {
        when (state) {
            is ChildGeneratingState<*, *> -> {
                state = state.parent
            }
            is FileGeneratingState -> {
                state.compiler.generators.advance(this)
            }
            else -> throw IllegalStateException("Root file state not found.")
        }
    }
}
