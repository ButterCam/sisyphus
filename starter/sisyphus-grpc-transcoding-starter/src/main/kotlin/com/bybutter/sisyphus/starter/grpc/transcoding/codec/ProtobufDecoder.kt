package com.bybutter.sisyphus.starter.grpc.transcoding.codec

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import kotlin.reflect.full.companionObjectInstance
import org.springframework.core.ResolvableType
import org.springframework.core.codec.AbstractDataBufferDecoder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.util.MimeType

class ProtobufDecoder(vararg mimeTypes: MimeType) : AbstractDataBufferDecoder<Message<*, *>>(*mimeTypes) {
    override fun decode(
        buffer: DataBuffer,
        targetType: ResolvableType,
        mimeType: MimeType?,
        hints: Map<String, Any>?
    ): Message<*, *> {
        val support = targetType.rawClass.kotlin.companionObjectInstance as MessageSupport<*, *>
        return support.parse(buffer.asInputStream(), Int.MAX_VALUE)
    }
}
