package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.string.toCamelCase
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

class OneofFieldDescriptor(
    override val parent: MessageDescriptor,
    override val descriptor: DescriptorProtos.OneofDescriptorProto
) : DescriptorNode<DescriptorProtos.OneofDescriptorProto>() {
    fun oneOfClassName(): ClassName {
        return parent.className().nestedClass(oneOfName())
    }

    fun oneOfName(): String {
        return descriptor.name.toPascalCase()
    }

    fun fieldName(): String {
        return descriptor.name.toCamelCase()
    }

    fun proto3Optional(): Boolean {
        val index = parent.descriptor.oneofDeclList.indexOf(descriptor)
        val fields = parent.descriptor.fieldList.filter { it.hasOneofIndex() && it.oneofIndex == index }
        return fields.size == 1 && fields.all { it.proto3Optional }
    }
}
