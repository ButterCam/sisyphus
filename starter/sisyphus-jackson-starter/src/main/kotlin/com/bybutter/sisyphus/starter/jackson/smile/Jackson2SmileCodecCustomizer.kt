package com.bybutter.sisyphus.starter.jackson.smile

import com.bybutter.sisyphus.jackson.Smile
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.http.codec.CodecConfigurer
import org.springframework.http.codec.json.Jackson2SmileDecoder
import org.springframework.http.codec.json.Jackson2SmileEncoder
import org.springframework.util.MimeType

class Jackson2SmileCodecCustomizer : CodecCustomizer {
    override fun customize(configurer: CodecConfigurer) {
        configurer.defaultCodecs().jackson2SmileEncoder(Jackson2SmileEncoder(Smile.mapper, *DEFAULT_SMILE_MIME_TYPES))
        configurer.defaultCodecs().jackson2SmileDecoder(Jackson2SmileDecoder(Smile.mapper, *DEFAULT_SMILE_MIME_TYPES))
    }

    companion object {
        val DEFAULT_SMILE_MIME_TYPES =
            arrayOf(
                MimeType("application", "x-jackson-smile"),
                MimeType("application", "*+x-jackson-smile"),
            )
    }
}
