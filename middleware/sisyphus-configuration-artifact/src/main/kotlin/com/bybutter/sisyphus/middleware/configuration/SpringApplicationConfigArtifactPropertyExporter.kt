package com.bybutter.sisyphus.middleware.configuration

object SpringApplicationConfigArtifactPropertyExporter : ConfigFormatFilePropertyExporter() {
    override val names: Collection<String> = listOf("spring/application")
}
