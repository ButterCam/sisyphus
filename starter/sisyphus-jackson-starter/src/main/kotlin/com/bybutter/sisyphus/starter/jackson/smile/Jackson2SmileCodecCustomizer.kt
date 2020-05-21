package com.bybutter.sisyphus.starter.jackson.smile

import com.bybutter.sisyphus.jackson.Smile
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.http.codec.CodecConfigurer
import org.springframework.http.codec.DecoderHttpMessageReader
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.http.codec.json.Jackson2SmileDecoder
import org.springframework.http.codec.json.Jackson2SmileEncoder

class Jackson2SmileCodecCustomizer : CodecCustomizer {
    override fun customize(configurer: CodecConfigurer) {
        configurer.customCodecs().register(EncoderHttpMessageWriter(Jackson2SmileEncoder(Smile.mapper)))
        configurer.customCodecs().register(DecoderHttpMessageReader(Jackson2SmileDecoder(Smile.mapper)))
    }
}
