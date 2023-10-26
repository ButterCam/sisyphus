package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.rpc.STATUS_META_KEY
import com.bybutter.sisyphus.security.base64
import io.grpc.ClientCall
import io.grpc.Metadata
import io.grpc.Status
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

class TranscodingStreamingCallListener(private val clientCall: ClientCall<*, *>) : TranscodingCallListener() {
    private val eventSink = Sinks.many().unicast().onBackpressureBuffer<ServerSentEvent<Any>>()
    private val response = Sinks.one<Mono<ServerResponse>>()

    override fun onClose(
        status: Status,
        trailers: Metadata,
    ) {
        try {
            val statusMessage =
                trailers[STATUS_META_KEY] ?: com.bybutter.sisyphus.rpc.Status {
                    this.code = status.code.value()
                    status.description?.let {
                        this.message = it
                    }
                }
            eventSink.tryEmitNext(
                ServerSentEvent.builder<Any>().event("status").data(statusMessage).build(),
            )
            eventSink.tryEmitNext(
                ServerSentEvent.builder<Any>().event("trailers").data(trailers.toMap()).build(),
            )
            eventSink.tryEmitComplete()
        } catch (e: Exception) {
            eventSink.tryEmitError(e)
        }
    }

    override fun onHeaders(headers: Metadata) {
        response.tryEmitValue(
            ServerResponse.status(HttpStatus.OK).setHeaderFromMetadata(headers)
                .body(BodyInserters.fromServerSentEvents(eventSink.asFlux())),
        )
    }

    override fun onMessage(message: Message<*, *>) {
        eventSink.tryEmitNext(
            ServerSentEvent.builder<Any>().event("message").data(message).build(),
        )
        clientCall.request(1)
    }

    private fun ServerResponse.BodyBuilder.setHeaderFromMetadata(metadata: Metadata?): ServerResponse.BodyBuilder {
        metadata ?: return this

        for (key in metadata.keys()) {
            // Skip ignore headers.
            if (IGNORE_GRPC_HEADER.contains(key)) continue
            // Skip bin headers.
            if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                header(key, metadata[Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER)]?.base64())
            } else {
                header(key, metadata[Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)])
            }
        }
        return this
    }

    private fun Metadata.toMap(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for (key in keys()) {
            // Skip ignore headers.
            if (IGNORE_GRPC_HEADER.contains(key)) continue
            // Skip bin headers.
            if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                this[Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER)]?.base64()?.let {
                    result[key] = it
                }
            } else {
                this[Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)]?.let {
                    result[key] = it
                }
            }
        }
        return result
    }

    override fun response(): Mono<ServerResponse> {
        return response.asMono().flatMap { it }
    }

    companion object {
        val IGNORE_GRPC_HEADER = setOf(HttpHeaders.CONTENT_TYPE.lowercase(), STATUS_META_KEY.name())
    }
}
