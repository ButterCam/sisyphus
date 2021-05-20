package com.bybutter.sisyphus.protobuf.compiler

import com.squareup.kotlinpoet.FileSpec

data class ProtoCompileResult(
    val descriptor: FileDescriptor,
    val files: List<GeneratedFile>
)

data class ProtoCompileResults(
    val booster: FileSpec?,
    val results: List<ProtoCompileResult>
)
