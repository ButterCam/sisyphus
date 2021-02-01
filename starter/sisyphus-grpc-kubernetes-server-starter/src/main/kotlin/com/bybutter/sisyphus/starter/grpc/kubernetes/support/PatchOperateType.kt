package com.bybutter.sisyphus.middleware.grpc.client.kubernetes.support

enum class PatchOperateType(val value: String) {
    UNSPECIFIED("patch_operate_type_unspecified"),
    REMOVE("remove"),
    ADD("add"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy")
}
