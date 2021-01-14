package com.bybutter.sisyphus.protobuf.compiler.generating

import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import java.nio.file.Paths


fun Generating<*, DescriptorProtos.FileDescriptorProto, *>.packageName(): String {
    return compiler().packageName(descriptor)
}

fun Generating<*, DescriptorProtos.FileDescriptorProto, *>.internalPackageName(): String {
    return compiler().internalPackageName(descriptor)
}

fun Generating<*, DescriptorProtos.FileDescriptorProto, *>.fileMetadataClassName(): ClassName {
    return ClassName.bestGuess("${internalPackageName()}.${fileMetadataName()}".trim('.'))
}

fun Generating<*, DescriptorProtos.FileDescriptorProto, *>.kotlinFileName(): String {
    return Paths.get(descriptor.name).toFile().nameWithoutExtension.toPascalCase()
}

fun Generating<*, DescriptorProtos.FileDescriptorProto, *>.fileMetadataName(): String {
    return "${kotlinFileName()}Metadata"
}