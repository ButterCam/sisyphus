package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger

import com.bybutter.sisyphus.middleware.configuration.ConfigFormatFilePropertyExporter

object SwaggerConfigArtifactPropertyExporter : ConfigFormatFilePropertyExporter() {
    override val names: Collection<String> = listOf("swagger/config")
}
