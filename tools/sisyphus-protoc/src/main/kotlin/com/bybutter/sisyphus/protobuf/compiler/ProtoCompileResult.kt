package com.bybutter.sisyphus.protobuf.compiler

import com.squareup.kotlinpoet.FileSpec

data class ProtoCompileResult(
    val descriptor: FileDescriptor,
    val files: List<FileSpec>
)
