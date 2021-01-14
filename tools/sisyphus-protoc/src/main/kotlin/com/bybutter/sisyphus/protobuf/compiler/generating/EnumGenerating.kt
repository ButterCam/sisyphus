package com.bybutter.sisyphus.protobuf.compiler.generating

import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

fun Generating<*, DescriptorProtos.EnumDescriptorProto, *>.name(): String {
    return descriptor.name
}

fun Generating<*, DescriptorProtos.EnumDescriptorProto, *>.supportName(): String {
    return "${name()}Support"
}

fun Generating<*, DescriptorProtos.EnumDescriptorProto, *>.className(): ClassName {
    return when(val parent = this.parent) {
        is MessageGenerating<*, *> -> {
            parent.className().nestedClass(name())
        }
        is FileGenerating<*, *> -> {
            ClassName(parent.packageName(), name())
        }
        else -> TODO()
    }
}

fun Generating<*, DescriptorProtos.EnumDescriptorProto, *>.supportClassName(): ClassName {
    return when(val parent = this.parent) {
        is MessageGenerating<*, *> -> {
            parent.supportClassName().nestedClass(supportName())
        }
        is FileGenerating<*, *> -> {
            ClassName(parent.internalPackageName(), supportName())
        }
        else -> TODO()
    }
}

fun Generating<*, DescriptorProtos.EnumDescriptorProto, *>.fullProtoName(): String {
    return when (val parent = this.parent) {
        is MessageGenerating<*, *> -> "${parent.fullProtoName()}.${descriptor.name}"
        is FileGenerating<*, *> -> "${parent.descriptor.`package`}.${descriptor.name}"
        else -> TODO()
    }
}