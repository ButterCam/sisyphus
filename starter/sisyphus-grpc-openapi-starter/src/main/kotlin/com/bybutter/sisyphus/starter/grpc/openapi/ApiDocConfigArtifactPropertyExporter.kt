package com.bybutter.sisyphus.starter.grpc.openapi

import com.bybutter.sisyphus.middleware.configuration.ConfigFormatFilePropertyExporter

/**
 *  The configuration of swagger uses 'swagger/config' by default.
 *  This configuration can be overridden in the application.
 * */
object ApiDocConfigArtifactPropertyExporter : ConfigFormatFilePropertyExporter() {
    override val names: Collection<String> = listOf("openapi/config")
}
