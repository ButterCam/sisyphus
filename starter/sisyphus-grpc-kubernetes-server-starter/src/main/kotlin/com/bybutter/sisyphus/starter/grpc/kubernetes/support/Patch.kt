package com.bybutter.sisyphus.middleware.grpc.client.kubernetes.support

interface Patch {
    var op: String
    var path: String
    var value: String
}
