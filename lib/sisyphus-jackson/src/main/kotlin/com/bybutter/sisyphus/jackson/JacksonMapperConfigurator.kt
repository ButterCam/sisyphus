package com.bybutter.sisyphus.jackson

import com.fasterxml.jackson.databind.ObjectMapper

interface JacksonMapperConfigurator {
    fun configure(mapper: ObjectMapper)
}
