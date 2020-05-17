package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto

interface ProtoFileMeta {
    val name: String
    val descriptor: FileDescriptorProto
}
