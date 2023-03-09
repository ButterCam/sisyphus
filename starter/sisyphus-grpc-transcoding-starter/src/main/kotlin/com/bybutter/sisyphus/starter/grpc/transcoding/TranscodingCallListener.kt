package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.protobuf.Message
import io.grpc.ClientCall
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

abstract class TranscodingCallListener : ClientCall.Listener<Message<*, *>>() {
    abstract fun response(): Mono<ServerResponse>
}
