package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.string.toCamelCase
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

class OneofFieldDescriptor(
    val parent: MessageDescriptor,
    override val descriptor: DescriptorProtos.OneofDescriptorProto
) : DescriptorNode<DescriptorProtos.OneofDescriptorProto> {
    fun oneOfClassName(): ClassName {
        return parent.className().nestedClass(oneOfName())
    }

    fun oneOfName(): String {
        return descriptor.name.toPascalCase()
    }

    fun fieldName(): String {
        return descriptor.name.toCamelCase()
    }
}
