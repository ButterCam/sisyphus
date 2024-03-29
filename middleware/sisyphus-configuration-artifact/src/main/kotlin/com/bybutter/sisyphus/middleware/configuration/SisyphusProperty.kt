package com.bybutter.sisyphus.middleware.configuration

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class SisyphusProperty(
    val environment: String = "",
    @NestedConfigurationProperty
    val repositories: Map<String, Repository> = mapOf(),
    @NestedConfigurationProperty
    val dependency: TargetRepositorySetting = TargetRepositorySetting(),
    @NestedConfigurationProperty
    val config: SisyphusConfigArtifacts = SisyphusConfigArtifacts(),
)

data class TargetRepositorySetting(
    val repositories: List<String> =
        listOf(
            "local",
            "central",
            "portal",
            "google",
        ),
)

data class Repository(
    val url: String,
    val username: String? = null,
    val password: String? = null,
)

data class SisyphusConfigArtifacts(
    val artifacts: List<String> = listOf(),
)
