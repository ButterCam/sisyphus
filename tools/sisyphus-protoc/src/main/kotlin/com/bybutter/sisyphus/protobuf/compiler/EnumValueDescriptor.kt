package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.string.toScreamingSnakeCase
import com.google.protobuf.DescriptorProtos

class EnumValueDescriptor(
    val parent: EnumDescriptor,
    override val descriptor: DescriptorProtos.EnumValueDescriptorProto
) : DescriptorNode<DescriptorProtos.EnumValueDescriptorProto> {
    fun name(): String {
        return descriptor.name.substringAfter("${parent.descriptor.name.toScreamingSnakeCase()}_")
    }
}
