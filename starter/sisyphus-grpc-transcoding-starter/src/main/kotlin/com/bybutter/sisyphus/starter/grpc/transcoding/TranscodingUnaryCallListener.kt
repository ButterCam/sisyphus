package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.primitives.Empty
import com.bybutter.sisyphus.rpc.Code
import com.bybutter.sisyphus.rpc.STATUS_META_KEY
import com.bybutter.sisyphus.security.base64
import com.bybutter.sisyphus.security.base64UrlSafe
import com.bybutter.sisyphus.starter.grpc.transcoding.util.toHttpStatus
import com.bybutter.sisyphus.starter.webflux.DetectBodyInserter
import io.grpc.Metadata
import io.grpc.Status
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

/**
 * Listener for gRPC calling, it convert gRPC call to [ServerResponse] asynchronously.
 * @property body String the response body field name of gRPC response message.
 * @constructor
 * @see https://aip.bybutter.com/127#http-method-and-path
 */
class TranscodingUnaryCallListener(private val body: String) : TranscodingCallListener() {
    private val response = Sinks.one<Mono<ServerResponse>>()

    private var headers: Metadata? = null
    private var message: Message<*, *>? = null

    override fun onClose(
        status: Status,
        trailers: Metadata,
    ) {
        try {
            val builder = ServerResponse.status(status.code.toHttpStatus())

            // Set response header from gRPC headers and trailers.
            builder.setHeaderFromMetadata(headers)
            builder.setHeaderFromMetadata(trailers)

            trailers[STATUS_META_KEY]?.let {
                if (it.code != Code.OK.number) {
                    // The trailers contains status meta and code is not [Code.OK], return the status directly.
                    this.response.tryEmitValue(builder.body(DetectBodyInserter(it)))
                    return
                } else {
                    // The trailers contains status meta but code is [Code.OK], return the status in response header.
                    builder.header(STATUS_META_KEY.name(), it.toProto().base64UrlSafe())
                }
            }

            val message = message
            val response =
                when {
                    // Empty message.
                    message is Empty -> builder.build()
                    // Return the body.
                    message != null ->
                        when (body) {
                            "", "*" -> {
                                builder.body(DetectBodyInserter(message))
                            }
                            else -> {
                                builder.body(DetectBodyInserter<Any>(message[body]))
                            }
                        }
                    // No message returned by gRPC, maybe some unknown error happened.
                    else ->
                        builder.body(
                            DetectBodyInserter(
                                com.bybutter.sisyphus.rpc.Status {
                                    this.code = status.code.value()
                                    this.message = status.description ?: status.cause?.message ?: "Unknown"
                                },
                            ),
                        )
                }
            this.response.tryEmitValue(response)
        } catch (e: Exception) {
            this.response.tryEmitValue(
                ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        DetectBodyInserter(
                            com.bybutter.sisyphus.rpc.Status {
                                this.code = Status.Code.INTERNAL.value()
                                this.message = e.message ?: "Unknown"
                            },
                        ),
                    ),
            )
        }
    }

    override fun onHeaders(headers: Metadata) {
        this.headers = headers
    }

    override fun onMessage(message: Message<*, *>) {
        this.message = message
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

    override fun response(): Mono<ServerResponse> {
        return response.asMono().flatMap { it }
    }

    companion object {
        val IGNORE_GRPC_HEADER = setOf(HttpHeaders.CONTENT_TYPE.lowercase(), STATUS_META_KEY.name())
    }
}
