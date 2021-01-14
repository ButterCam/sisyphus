package com.bybutter.sisyphus.protobuf.compiler.generating

import com.bybutter.sisyphus.string.toScreamingSnakeCase
import com.google.protobuf.DescriptorProtos

fun Generating<out Generating<*, DescriptorProtos.EnumDescriptorProto, *>, DescriptorProtos.EnumValueDescriptorProto, *>.name(): String {
    return descriptor.name.substringAfter("${parent.descriptor.name.toScreamingSnakeCase()}_")
}