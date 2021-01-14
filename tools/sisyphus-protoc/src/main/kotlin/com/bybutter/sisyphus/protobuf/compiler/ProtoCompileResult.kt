package com.bybutter.sisyphus.protobuf.compiler

import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

data class ProtoCompileResult(
    val descriptor: DescriptorProtos.FileDescriptorProto,
    val files: List<FileSpec>
)