package com.bybutter.sisyphus.starter.jackson.json

import com.bybutter.sisyphus.jackson.Json
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.http.codec.CodecConfigurer
import org.springframework.http.codec.DecoderHttpMessageReader
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder

class Jackson2JsonCodecCustomizer : CodecCustomizer {
    override fun customize(configurer: CodecConfigurer) {
        configurer.customCodecs().register(EncoderHttpMessageWriter(Jackson2JsonEncoder(Json.mapper)))
        configurer.customCodecs().register(DecoderHttpMessageReader(Jackson2JsonDecoder(Json.mapper)))
    }
}
