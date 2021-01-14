package com.bybutter.sisyphus.protobuf.compiler.generating

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

fun Generating<*, DescriptorProtos.ServiceDescriptorProto, *>.fullProtoName(): String {
    return "${file().descriptor.`package`}.${descriptor.name}"
}

fun Generating<*, DescriptorProtos.ServiceDescriptorProto, *>.name(): String {
    return descriptor.name
}

fun Generating<*, DescriptorProtos.ServiceDescriptorProto, *>.className(): ClassName {
    return ClassName(file().packageName(), name())
}

fun Generating<*, DescriptorProtos.ServiceDescriptorProto, *>.supportName(): String {
    return "${descriptor.name}Support"
}

fun Generating<*, DescriptorProtos.ServiceDescriptorProto, *>.supportClassName(): ClassName {
    return ClassName(file().internalPackageName(), supportName())
}

fun Generating<*, DescriptorProtos.ServiceDescriptorProto, *>.path(): List<Int> {
    return listOf(DescriptorProtos.FileDescriptorProto.SERVICE_FIELD_NUMBER, file().descriptor.serviceList.indexOf(descriptor))
}

fun Generating<*, DescriptorProtos.ServiceDescriptorProto, *>.document(): String {
    return escapeDoc(file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
        it.pathList.contentEquals(path())
    }?.leadingComments ?: "")
}
