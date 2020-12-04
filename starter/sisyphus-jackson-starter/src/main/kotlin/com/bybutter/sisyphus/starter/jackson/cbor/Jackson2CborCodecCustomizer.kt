package com.bybutter.sisyphus.starter.jackson.cbor

import com.bybutter.sisyphus.jackson.Cbor
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.http.MediaType
import org.springframework.http.codec.CodecConfigurer
import org.springframework.http.codec.DecoderHttpMessageReader
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder

class Jackson2CborCodecCustomizer : CodecCustomizer {
    override fun customize(configurer: CodecConfigurer) {
        configurer.customCodecs().register(EncoderHttpMessageWriter(Jackson2CborEncoder(Cbor.mapper, MediaType.APPLICATION_CBOR)))
        configurer.customCodecs().register(DecoderHttpMessageReader(Jackson2CborDecoder(Cbor.mapper, MediaType.APPLICATION_CBOR)))
    }
}
