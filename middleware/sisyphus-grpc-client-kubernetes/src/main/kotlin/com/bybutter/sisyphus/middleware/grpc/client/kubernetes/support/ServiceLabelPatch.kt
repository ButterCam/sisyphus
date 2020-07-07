package com.bybutter.sisyphus.middleware.grpc.client.kubernetes.support

class ServiceLabelPatch : Patch {
    override var op: String
    override var path: String
    override var value: String

    constructor(operateType: PatchOperateType, path: String, value: String) {
        this.op = operateType.value
        this.path = "/metadata/labels/$path"
        this.value = value
    }
}
