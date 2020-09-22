package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger

import com.bybutter.sisyphus.middleware.configuration.ConfigFormatFilePropertyExporter

/**
 *  The configuration of swagger uses 'swagger/config' by default.
 *  This configuration can be overridden in the application.
 * */
object SwaggerConfigArtifactPropertyExporter : ConfigFormatFilePropertyExporter() {
    override val names: Collection<String> = listOf("swagger/config")
}
