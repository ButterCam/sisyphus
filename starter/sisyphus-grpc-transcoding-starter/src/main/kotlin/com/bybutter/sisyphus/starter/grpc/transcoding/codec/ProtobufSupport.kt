package com.bybutter.sisyphus.starter.grpc.transcoding.codec

import org.springframework.util.MimeType

abstract class ProtobufSupport {
    protected fun supportsMimeType(mimeType: MimeType?): Boolean {
        return mimeType == null || ProtobufCodecCustomizer.MIME_TYPES.any { it.isCompatibleWith(mimeType) }
    }
}
