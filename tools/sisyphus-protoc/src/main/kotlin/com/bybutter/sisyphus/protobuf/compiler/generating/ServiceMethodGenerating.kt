package com.bybutter.sisyphus.protobuf.compiler.generating

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.bybutter.sisyphus.string.toCamelCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName

fun Generating<*, DescriptorProtos.MethodDescriptorProto, *>.name(): String {
    return descriptor.name.toCamelCase()
}

fun Generating<out Generating<*, DescriptorProtos.ServiceDescriptorProto, *>, DescriptorProtos.MethodDescriptorProto, *>.fullProtoName(): String {
    return "${parent.fullProtoName()}/${descriptor.name}"
}

fun Generating<out Generating<*, DescriptorProtos.ServiceDescriptorProto, *>, DescriptorProtos.MethodDescriptorProto, *>.path(): List<Int> {
    return parent.path() + listOf(DescriptorProtos.ServiceDescriptorProto.METHOD_FIELD_NUMBER, parent.descriptor.methodList.indexOf(descriptor))
}

fun Generating<out Generating<*, DescriptorProtos.ServiceDescriptorProto, *>, DescriptorProtos.MethodDescriptorProto, *>.document(): String {
    return escapeDoc(file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
        it.pathList.contentEquals(path())
    }?.leadingComments ?: "")
}

fun Generating<*, DescriptorProtos.MethodDescriptorProto, *>.inputClassName(): ClassName {
    return compiler().protoClassName(descriptor.inputType)
}

fun Generating<*, DescriptorProtos.MethodDescriptorProto, *>.outputClassName(): ClassName {
    return compiler().protoClassName(descriptor.outputType)
}