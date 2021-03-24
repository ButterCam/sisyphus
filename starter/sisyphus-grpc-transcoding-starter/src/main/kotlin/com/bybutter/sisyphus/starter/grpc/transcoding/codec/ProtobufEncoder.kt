package com.bybutter.sisyphus.starter.grpc.transcoding.codec

import com.bybutter.sisyphus.protobuf.Message
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.codec.AbstractEncoder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.codec.HttpMessageEncoder
import org.springframework.util.MimeType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.IOException

class ProtobufEncoder(vararg mimeTypes: MimeType) :
    AbstractEncoder<Message<*, *>>(*mimeTypes),
    HttpMessageEncoder<Message<*, *>> {
    override fun getStreamingMediaTypes(): List<MediaType> {
        return ProtobufCodecCustomizer.STREAM_MIME_TYPES
    }

    override fun encode(
        inputStream: Publisher<out Message<*, *>>,
        bufferFactory: DataBufferFactory,
        elementType: ResolvableType,
        mimeType: MimeType?,
        hints: Map<String, Any>?
    ): Flux<DataBuffer> {
        return Flux.from(inputStream).map<DataBuffer> {
            encodeValue(it, bufferFactory, inputStream !is Mono<*>)
        }
    }

    override fun encodeValue(
        value: Message<*, *>,
        bufferFactory: DataBufferFactory,
        valueType: ResolvableType,
        mimeType: MimeType?,
        hints: MutableMap<String, Any>?
    ): DataBuffer {
        return encodeValue(value, bufferFactory, false)
    }

    private fun encodeValue(message: Message<*, *>, bufferFactory: DataBufferFactory, delimited: Boolean): DataBuffer {
        val buffer = bufferFactory.allocateBuffer()
        var release = true
        return try {
            if (delimited) {
                message.writeDelimitedTo(buffer.asOutputStream())
            } else {
                message.writeTo(buffer.asOutputStream())
            }
            release = false
            buffer
        } catch (ex: IOException) {
            throw IllegalStateException("Unexpected I/O error while writing to data buffer", ex)
        } finally {
            if (release) {
                DataBufferUtils.release(buffer)
            }
        }
    }
}
