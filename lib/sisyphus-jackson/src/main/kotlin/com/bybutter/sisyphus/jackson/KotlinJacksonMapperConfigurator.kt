package com.bybutter.sisyphus.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object KotlinJacksonMapperConfigurator : JacksonMapperConfigurator {
    override fun configure(mapper: ObjectMapper) {
        mapper.registerKotlinModule()
    }
}
