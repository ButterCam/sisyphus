package com.bybutter.sisyphus.starter.grpc.transcoding.codec

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.ProtobufDefinition
import com.bybutter.sisyphus.protobuf.findMessageSupport
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
        val fullName = targetType.rawClass.getAnnotation(ProtobufDefinition::class.java).name
        return ProtoTypes.findMessageSupport(fullName).parse(buffer.asInputStream(), Int.MAX_VALUE)
    }
}
