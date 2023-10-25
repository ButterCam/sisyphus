package com.bybutter.sisyphus.starter.jackson.yaml

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.codec.json.AbstractJackson2Decoder
import org.springframework.util.MimeType

class Jackson2YamlDecoder(objectMapper: ObjectMapper, vararg mimeTypes: MimeType) :
    AbstractJackson2Decoder(
        objectMapper,
        *(if (mimeTypes.isEmpty()) Jackson2YamlCodecCustomizer.DEFAULT_YAML_MIME_TYPES else mimeTypes),
    )
