package com.bybutter.sisyphus.starter.grpc.support

import io.grpc.Metadata
import io.grpc.ServerCall

interface IncomingRequestLogger : RequestLogger {
    fun log(
        call: ServerCall<*, *>,
        inputHeader: Metadata,
    )
}
