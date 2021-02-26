package com.bybutter.sisyphus.protobuf.compiler

abstract class DescriptorNode<T> {
    abstract val parent: DescriptorNode<*>

    abstract val descriptor: T

    private var children = listOf<DescriptorNode<*>>()

    fun resolve() {
        val children = mutableListOf<DescriptorNode<*>>()
        resolveChildren(children)
        for (child in children) {
            child.resolve()
        }
        this.children = children
    }

    protected open fun resolveChildren(children: MutableList<DescriptorNode<*>>) {
        DescriptorResolver.resolve(this, children)
    }

    fun children(): List<DescriptorNode<*>> {
        return children
    }
}

fun DescriptorNode<*>.file(): FileDescriptor {
    var state: DescriptorNode<*> = this
    while (true) {
        state = when (state) {
            is FileDescriptor -> return state
            is ServiceDescriptor -> return state.parent
            is MethodDescriptor -> return state.parent.parent
            else -> state.parent
        }
    }
}

fun DescriptorNode<*>.fileSet(): FileDescriptorSet {
    var state: DescriptorNode<*> = this
    while (true) {
        state = when (state) {
            is FileDescriptorSet -> return state
            is FileDescriptor -> return state.parent
            is ServiceDescriptor -> return state.parent.parent
            is MethodDescriptor -> return state.parent.parent.parent
            else -> state.parent
        }
    }
}
