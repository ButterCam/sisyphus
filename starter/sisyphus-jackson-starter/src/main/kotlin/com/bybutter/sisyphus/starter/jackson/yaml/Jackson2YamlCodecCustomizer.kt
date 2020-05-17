package com.bybutter.sisyphus.starter.jackson.yaml

import com.bybutter.sisyphus.jackson.Yaml
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.http.codec.CodecConfigurer
import org.springframework.http.codec.DecoderHttpMessageReader
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.stereotype.Component
import org.springframework.util.MimeType

@Component
class Jackson2YamlCodecCustomizer : CodecCustomizer {
    companion object {
        val DEFAULT_YAML_MIME_TYPES = arrayOf(
                MimeType("text", "vnd.yaml"),
                MimeType("text", "yaml"),
                MimeType("text", "x-yaml"),
                MimeType("text", "*+x-yaml"),
                MimeType("application", "yaml"),
                MimeType("application", "x-yaml"),
                MimeType("application", "*+x-yaml"))
    }

    override fun customize(configurer: CodecConfigurer) {
        configurer.customCodecs().register(EncoderHttpMessageWriter(Jackson2YamlEncoder(Yaml.mapper)))
        configurer.customCodecs().register(DecoderHttpMessageReader(Jackson2YamlDecoder(Yaml.mapper)))
    }
}
