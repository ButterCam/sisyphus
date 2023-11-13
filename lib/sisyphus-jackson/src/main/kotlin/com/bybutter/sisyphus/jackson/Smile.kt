package com.bybutter.sisyphus.jackson

import com.bybutter.sisyphus.spi.ServiceLoader
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory

object Smile : JacksonFormatSupport() {
    override val mapper: ObjectMapper by lazy {
        val mapper = ObjectMapper(SmileFactory())
        ServiceLoader.load(JacksonMapperConfigurator::class.java).forEach {
            try {
                it.configure(mapper)
            } catch (_: Exception) {
            }
        }
        mapper.findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.IGNORE_UNDEFINED, true)
            .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
    }
}
