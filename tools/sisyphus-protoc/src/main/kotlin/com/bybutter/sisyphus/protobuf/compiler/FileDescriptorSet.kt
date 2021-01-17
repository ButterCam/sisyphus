package com.bybutter.sisyphus.protobuf.compiler

import com.google.protobuf.DescriptorProtos

class FileDescriptorSet(
    override val descriptor: DescriptorProtos.FileDescriptorSet,
    private val packageShading: Map<String, String>
) : DescriptorNode<DescriptorProtos.FileDescriptorSet>() {

    private val lookupTable: MutableMap<String, DescriptorNode<*>> = mutableMapOf()

    override fun resolveChildren(children: MutableList<DescriptorNode<*>>) {
        children += descriptor.fileList.map {
            FileDescriptor(this, it)
        }
        super.resolveChildren(children)
    }

    override val parent: DescriptorNode<*> get() = this

    val files: List<FileDescriptor> get() = children().filterIsInstance<FileDescriptor>()

    fun shadePackage(packageName: String): String {
        return packageShading[packageName] ?: packageName
    }

    fun ensureMessage(proto: String): MessageDescriptor {
        return findMessage(proto) ?: throw IllegalStateException("Definition of message '$proto' not found")
    }

    fun ensureEnum(proto: String): EnumDescriptor {
        return findEnum(proto) ?: throw IllegalStateException("Definition of enum '$proto' not found")
    }

    fun findMessage(proto: String): MessageDescriptor? {
        return lookup(proto) as? MessageDescriptor
    }

    fun findEnum(proto: String): EnumDescriptor? {
        return lookup(proto) as? EnumDescriptor
    }

    fun lookup(name: String) : DescriptorNode<*>? {
        return lookupTable[name]
    }

    fun registerLookup(name: String, node: DescriptorNode<*>) {
        lookupTable[name] = node
    }
}
