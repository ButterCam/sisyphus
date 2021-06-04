package com.bybutter.sisyphus.protobuf

object ProtoTypes : LocalProtoReflection() {
    init {
        ProtobufBooster.boost()
    }
}
