package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.bybutter.sisyphus.string.toScreamingSnakeCase
import com.google.protobuf.DescriptorProtos

class EnumValueDescriptor(
    override val parent: EnumDescriptor,
    override val descriptor: DescriptorProtos.EnumValueDescriptorProto,
) : DescriptorNode<DescriptorProtos.EnumValueDescriptorProto>() {
    fun name(): String {
        return descriptor.name.substringAfter("${parent.descriptor.name.toScreamingSnakeCase()}_")
    }

    fun path(): List<Int> {
        return parent.path() + DescriptorProtos.EnumDescriptorProto.VALUE_FIELD_NUMBER +
            parent.descriptor.valueList.indexOf(
                descriptor,
            )
    }

    fun document(): String {
        return escapeDoc(
            file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
                it.pathList.contentEquals(path())
            }?.leadingComments ?: "",
        )
    }
}
