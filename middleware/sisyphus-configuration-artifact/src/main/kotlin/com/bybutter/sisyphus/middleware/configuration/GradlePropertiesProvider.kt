package com.bybutter.sisyphus.middleware.configuration

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertiesPropertySource

/**
 * Inject all fields in 'gradle.properties' into Spring runtime environment.
 */
class GradlePropertiesProvider : EnvironmentPostProcessor {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        registerRootGradleProperties(environment)
        registerWorkspaceGradleProperties(environment)
    }

    private fun registerRootGradleProperties(environment: ConfigurableEnvironment) {
        registerProperties("rootGradleProperties",
            Path.of(System.getProperty("user.home"), ".gradle", GRADLE_PROPERTIES),
            environment)
    }

    private fun registerWorkspaceGradleProperties(environment: ConfigurableEnvironment) {
        registerProperties("rootGradleProperties",
            Path.of(System.getProperty("user.dir"), GRADLE_PROPERTIES),
            environment)
    }

    private fun registerProperties(name: String, path: Path, environment: ConfigurableEnvironment) {
        if (!Files.exists(path)) {
            return
        }

        logger.debug("Properties injected via gradle properties at '$path'.")

        val properties = Files.newInputStream(path).use {
            Properties().apply {
                this.load(it)
            }
        }

        environment.propertySources.addFirst(PropertiesPropertySource(name, properties))
    }

    companion object {
        const val GRADLE_PROPERTIES = "gradle.properties"

        private val logger = LoggerFactory.getLogger(GradlePropertiesProvider::class.java)
    }
}
