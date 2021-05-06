package com.bybutter.sisyphus.protobuf.compiler

data class ProtoCompileResult(
    val descriptor: FileDescriptor,
    val files: List<GeneratedFile>
)
