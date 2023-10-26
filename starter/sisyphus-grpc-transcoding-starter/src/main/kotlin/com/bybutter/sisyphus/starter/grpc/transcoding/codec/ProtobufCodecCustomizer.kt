package com.bybutter.sisyphus.starter.grpc.transcoding.codec

import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.http.MediaType
import org.springframework.http.codec.CodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.util.MimeType

/**
 * Add the protobuf format in WebFlux server, it add protobuf encoding/decoding for HTTP requests.
 */
@Component
class ProtobufCodecCustomizer : CodecCustomizer {
    companion object {
        /**
         * The supported mime-types, `application/x-protobuf` and `application/octet-stream` are supported.
         */
        val MIME_TYPES =
            arrayOf(
                MimeType("application", "x-protobuf"),
                MimeType("application", "octet-stream"),
            )

        val STREAM_MIME_TYPES =
            MIME_TYPES.map {
                MediaType(it.type, it.subtype, mapOf("delimited" to "true"))
            }
    }

    override fun customize(configurer: CodecConfigurer) {
        configurer.customCodecs().registerWithDefaultConfig(ProtobufEncoder(*MIME_TYPES))
        configurer.customCodecs().registerWithDefaultConfig(ProtobufDecoder(*MIME_TYPES))
    }
}
