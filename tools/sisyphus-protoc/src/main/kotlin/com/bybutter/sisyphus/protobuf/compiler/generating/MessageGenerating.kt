package com.bybutter.sisyphus.protobuf.compiler.generating

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

fun Generating<*, DescriptorProtos.DescriptorProto, *>.fullProtoName(): String {
    return when (val parent = this.parent) {
        is MessageGenerating<*, *> -> "${parent.fullProtoName()}.${descriptor.name}"
        is FileGenerating<*, *> -> "${parent.descriptor.`package`}.${descriptor.name}"
        else -> TODO()
    }
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.name(): String {
    return descriptor.name
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.mutableName(): String {
    return "Mutable${name()}"
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.implementationName(): String {
    return "${name()}Impl"
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.supportName(): String {
    return "${name()}Support"
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.className(): ClassName {
    return when (val parent = this.parent) {
        is MessageGenerating<*, *> -> {
            parent.className().nestedClass(name())
        }
        is FileGenerating<*, *> -> {
            ClassName(parent.packageName(), name())
        }
        else -> TODO()
    }
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.mutableClassName(): ClassName {
    return when (val parent = this.parent) {
        is MessageGenerating<*, *> -> {
            parent.mutableClassName().nestedClass(mutableName())
        }
        is FileGenerating<*, *> -> {
            ClassName(parent.internalPackageName(), mutableName())
        }
        else -> TODO()
    }
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.implementationClassName(): ClassName {
    return when (val parent = this.parent) {
        is MessageGenerating<*, *> -> {
            parent.implementationClassName().nestedClass(implementationName())
        }
        is FileGenerating<*, *> -> {
            ClassName(parent.internalPackageName(), implementationName())
        }
        else -> TODO()
    }
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.supportClassName(): ClassName {
    return when (val parent = this.parent) {
        is MessageGenerating<*, *> -> {
            parent.supportClassName().nestedClass(supportName())
        }
        is FileGenerating<*, *> -> {
            ClassName(parent.internalPackageName(), supportName())
        }
        else -> TODO()
    }
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.path(): List<Int> {
    val path = mutableListOf<Int>()
    var state: Any? = this
    while (state is MessageGenerating<*, *>) {
        when (val parent = this.parent) {
            is MessageGenerating<*, *> -> {
                path += parent.descriptor.nestedTypeList.indexOf(state.descriptor)
                path += DescriptorProtos.DescriptorProto.NESTED_TYPE_FIELD_NUMBER
            }
            is FileGenerating<*, *> -> {
                path += parent.descriptor.messageTypeList.indexOf(state.descriptor)
                path += DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER
            }
        }
        state = state.parent
    }
    path.reverse()
    return path
}

fun Generating<*, DescriptorProtos.DescriptorProto, *>.document(): String {
    return escapeDoc(file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
        it.pathList.contentEquals(path())
    }?.leadingComments ?: "")
}
