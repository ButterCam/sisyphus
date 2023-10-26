package com.bybutter.sisyphus.starter.grpc.transcoding

import io.grpc.Metadata
import org.springframework.web.reactive.function.server.ServerRequest

interface TranscodingHeaderExporter {
    fun export(
        request: ServerRequest,
        metadata: Metadata,
    )
}
