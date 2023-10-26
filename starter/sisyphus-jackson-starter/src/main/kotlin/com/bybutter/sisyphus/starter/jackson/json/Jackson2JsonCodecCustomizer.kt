package com.bybutter.sisyphus.starter.jackson.json

import com.bybutter.sisyphus.jackson.Json
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.http.codec.CodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder

class Jackson2JsonCodecCustomizer : CodecCustomizer {
    override fun customize(configurer: CodecConfigurer) {
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(Json.mapper))
        configurer.defaultCodecs().jackson2JsonDecoder(
            Jackson2JsonDecoder(Json.mapper).apply {
                this.maxInMemorySize = 4 * 1024 * 1024
            },
        )
    }
}
