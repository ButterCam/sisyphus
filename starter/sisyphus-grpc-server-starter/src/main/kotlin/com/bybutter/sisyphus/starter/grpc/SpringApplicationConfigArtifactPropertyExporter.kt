package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.middleware.configuration.ConfigFormatFilePropertyExporter

object SpringApplicationConfigArtifactPropertyExporter : ConfigFormatFilePropertyExporter() {
    override val names: Collection<String> = listOf("spring/application")
}
