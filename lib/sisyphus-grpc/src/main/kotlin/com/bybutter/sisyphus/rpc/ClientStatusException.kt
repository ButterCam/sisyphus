package com.bybutter.sisyphus.rpc

import io.grpc.Metadata

open class ClientStatusException(status: io.grpc.Status, val trailers: Metadata) :
    RuntimeException(status.description, status.cause) {
    val status: Status =
        kotlin.run {
            trailers[STATUS_META_KEY] ?: Status {
                this.code = status.code.value()
                status.description?.let { this.message = it }
            }
        }
}
