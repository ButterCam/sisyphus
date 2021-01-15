package com.bybutter.sisyphus.protobuf.compiler

interface DescriptorNode<T> {
    val descriptor: T
}

fun DescriptorNode<*>.file(): FileDescriptor {
    var state: Any? = this
    while (true) {
        state = when (state) {
            is FileDescriptor -> return state
            is MessageDescriptor -> state.parent
            is MessageFieldDescriptor -> state.parent
            is OneofFieldDescriptor -> state.parent
            is EnumDescriptor -> state.parent
            is EnumValueDescriptor -> state.parent
            is ServiceDescriptor -> return state.parent
            is MethodDescriptor -> return state.parent.parent
            is ExtensionDescriptor -> state.parent
            else -> throw IllegalStateException("Unknown descriptor")
        }
    }
}

fun DescriptorNode<*>.fileSet(): FileDescriptorSet {
    var state: Any? = this
    while (true) {
        state = when (state) {
            is FileDescriptorSet -> return state
            is FileDescriptor -> return state.parent
            is MessageDescriptor -> state.parent
            is MessageFieldDescriptor -> state.parent
            is OneofFieldDescriptor -> state.parent
            is EnumDescriptor -> state.parent
            is EnumValueDescriptor -> state.parent
            is ServiceDescriptor -> return state.parent.parent
            is MethodDescriptor -> return state.parent.parent.parent
            is ExtensionDescriptor -> state.parent
            else -> throw IllegalStateException("Unknown descriptor")
        }
    }
}
