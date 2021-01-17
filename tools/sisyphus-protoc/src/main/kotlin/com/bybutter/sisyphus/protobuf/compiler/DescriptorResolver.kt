package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.spi.ServiceLoader

interface DescriptorResolver {
    fun resolve(descriptor: DescriptorNode<*>, children: MutableList<DescriptorNode<*>>)

    companion object {
        private val resolvers: List<DescriptorResolver> by lazy {
            ServiceLoader.load(DescriptorResolver::class.java)
        }

        fun resolve(descriptor: DescriptorNode<*>, children: MutableList<DescriptorNode<*>>) {
            for (it in resolvers) {
                it.resolve(descriptor, children)
            }
        }
    }
}