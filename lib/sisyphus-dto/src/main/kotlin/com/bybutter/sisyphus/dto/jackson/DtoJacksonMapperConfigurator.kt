package com.bybutter.sisyphus.dto.jackson

import com.bybutter.sisyphus.jackson.JacksonMapperConfigurator
import com.fasterxml.jackson.databind.ObjectMapper

object DtoJacksonMapperConfigurator : JacksonMapperConfigurator {
    override fun configure(mapper: ObjectMapper) {
        mapper.registerModule(DtoModule())
    }
}
