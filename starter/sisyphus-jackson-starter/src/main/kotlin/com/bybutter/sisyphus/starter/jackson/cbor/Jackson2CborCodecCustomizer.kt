package com.bybutter.sisyphus.starter.jackson.cbor

import com.bybutter.sisyphus.jackson.Cbor
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.http.codec.CodecConfigurer
import org.springframework.http.codec.DecoderHttpMessageReader
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(CBORFactory::class)
class Jackson2CborCodecCustomizer : CodecCustomizer {
    override fun customize(configurer: CodecConfigurer) {
        configurer.customCodecs().register(EncoderHttpMessageWriter(Jackson2CborEncoder(Cbor.mapper)))
        configurer.customCodecs().register(DecoderHttpMessageReader(Jackson2CborDecoder(Cbor.mapper)))
    }
}
