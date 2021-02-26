package com.bybutter.sisyphus.starter.jackson.yaml

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.codec.json.AbstractJackson2Encoder
import org.springframework.util.MimeType

class Jackson2YamlEncoder(objectMapper: ObjectMapper, vararg mimeTypes: MimeType) :
    AbstractJackson2Encoder(
        objectMapper,
        *(if (mimeTypes.isEmpty()) Jackson2YamlCodecCustomizer.DEFAULT_YAML_MIME_TYPES else mimeTypes)
    )
