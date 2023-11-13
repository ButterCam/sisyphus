package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.jackson.JacksonMapperConfigurator
import com.fasterxml.jackson.databind.ObjectMapper

object ProtobufJacksonMapperConfigurator : JacksonMapperConfigurator {
    override fun configure(mapper: ObjectMapper) {
        mapper.registerModule(ProtoModule())
    }
}
