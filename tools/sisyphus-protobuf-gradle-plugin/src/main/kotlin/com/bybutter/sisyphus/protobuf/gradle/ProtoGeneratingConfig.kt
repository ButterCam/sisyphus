package com.bybutter.sisyphus.protobuf.gradle

data class ProtoGeneratingConfig(
    var srcDir: String? = null,
    var outputDir: String? = null,
    var metadataDir: String? = null
)
