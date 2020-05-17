package com.bybutter.sisyphus.starter.grpc.transcoding.codec

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoSupport
import com.google.protobuf.CodedInputStream
import kotlin.reflect.full.companionObjectInstance
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.codec.Decoder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferLimitException
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.util.MimeType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ProtobufDecoder : ProtobufSupport(), Decoder<Message<*, *>> {
    companion object {
        const val DEFAULT_MESSAGE_MAX_SIZE = 256 * 1024
    }

    var maxMessageSize = DEFAULT_MESSAGE_MAX_SIZE

    override fun decodeToMono(
        inputStream: Publisher<DataBuffer>,
        elementType: ResolvableType,
        mimeType: MimeType?,
        hints: Map<String, Any>?
    ): Mono<Message<*, *>> {
        return DataBufferUtils.join(inputStream, maxMessageSize).map {
            decode(it, elementType, mimeType, hints)
        }
    }

    override fun getDecodableMimeTypes(): List<MimeType> {
        return ProtobufCodecCustomizer.MIME_TYPES
    }

    override fun canDecode(elementType: ResolvableType, mimeType: MimeType?): Boolean {
        return Message::class.java.isAssignableFrom(elementType.toClass()) && supportsMimeType(mimeType)
    }

    override fun decode(
        inputStream: Publisher<DataBuffer>,
        elementType: ResolvableType,
        mimeType: MimeType?,
        hints: Map<String, Any>?
    ): Flux<Message<*, *>> {
        return Flux.from(inputStream).flatMapIterable {
            StreamingMessageIterator(it, elementType, maxMessageSize).asSequence().asIterable()
        }
    }

    override fun decode(buffer: DataBuffer, targetType: ResolvableType, mimeType: MimeType?, hints: Map<String, Any>?): Message<*, *> {
        val support = targetType.rawClass.kotlin.companionObjectInstance as ProtoSupport<*, *>
        return support.parse(buffer.asInputStream())
    }
}

private class StreamingMessageIterator(private val buffer: DataBuffer, private val elementType: ResolvableType, private val maxMessageSize: Int) : Iterator<Message<*, *>> {
    private val input = CodedInputStream.newInstance(buffer.asInputStream())

    override fun hasNext(): Boolean {
        if (input.isAtEnd) {
            DataBufferUtils.release(buffer)
        }
        return !input.isAtEnd
    }

    override fun next(): Message<*, *> {
        val size = input.readInt32()
        if (maxMessageSize in 1 until size) {
            throw DataBufferLimitException(
                    "The number of bytes to read for message ($maxMessageSize) exceeds the configured limit ($maxMessageSize)")
        }

        val support = elementType.rawClass.kotlin.companionObjectInstance as ProtoSupport<*, *>
        return support.parse(input, size)
    }
}
