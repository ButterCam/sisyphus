package com.bybutter.sisyphus.middleware.configuration

import com.bybutter.sisyphus.middleware.configuration.maven.Aether
import com.bybutter.sisyphus.spi.ServiceLoader
import java.net.URLClassLoader
import org.eclipse.aether.resolution.ArtifactResult
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.logging.DeferredLog
import org.springframework.context.ApplicationListener
import org.springframework.core.env.ConfigurableEnvironment

/**
 * Provide config artifacts for [ConfigArtifactPropertyExporter], it will download artifacts from configured maven repository
 * and load it dynamically.
 */
class ConfigArtifactProvider : EnvironmentPostProcessor, ApplicationListener<ApplicationPreparedEvent> {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val binder = Binder(ConfigurationPropertySources.from(environment.propertySources))
        val properties = binder.bind("sisyphus", SisyphusProperty::class.java)
            .orElse(null) ?: return

        if (properties.config.artifacts.isEmpty()) {
            logger.warn("Skip load config artifacts due to empty artifacts list, artifacts list can be set by 'sisyphus.config.artifacts' property.")
            return
        }

        for (repositoryKey in properties.dependency.repositories) {
            val repository = when (repositoryKey) {
                "local" -> properties.repositories[repositoryKey] ?: run {
                    aether.registerLocal()
                    null
                }
                "central" -> properties.repositories[repositoryKey] ?: run {
                    aether.registerMavenCentral()
                    null
                }
                "jcenter" -> properties.repositories[repositoryKey] ?: run {
                    aether.registerJCenter()
                    null
                }
                "portal" -> properties.repositories[repositoryKey] ?: run {
                    aether.registerGradlePortal()
                    null
                }
                "google" -> properties.repositories[repositoryKey] ?: run {
                    aether.registerGoogle()
                    null
                }
                else -> properties.repositories[repositoryKey]
            }

            repository ?: continue
            aether.registerRepository(repository.url, repository.username, repository.password)
        }

        val artifacts = properties.config.artifacts.flatMap {
            try {
                logger.debug("Resolving config artifact '$it'.")
                aether.resolveDependencies(it).artifactResults
            } catch (e: Exception) {
                logger.error("Can't resolve config artifact '$it', cause by exception", e)
                listOf<ArtifactResult>()
            }
        }

        val jarFiles = artifacts.map {
            it.artifact.file.toURI().toURL()
        }.distinct()

        logger.info("${jarFiles.size} config artifacts loaded: ${artifacts.joinToString(", ") { "'${it.artifact}'" }}.")

        val classLoader = URLClassLoader(jarFiles.toTypedArray(), ConfigArtifactProvider::class.java.classLoader)
        val propertySources = ServiceLoader.load(ConfigArtifactPropertyExporter::class.java)
            .sortedByDescending { it.order }
            .flatMap {
                it.export(properties.environment, classLoader)
            }

        for (propertySource in propertySources) {
            environment.propertySources.addFirst(propertySource)
        }
    }

    override fun onApplicationEvent(event: ApplicationPreparedEvent) {
        logger.switchTo(ConfigArtifactProvider::class.java)
    }

    companion object {
        private val aether = Aether()

        private val logger = DeferredLog()
    }
}
