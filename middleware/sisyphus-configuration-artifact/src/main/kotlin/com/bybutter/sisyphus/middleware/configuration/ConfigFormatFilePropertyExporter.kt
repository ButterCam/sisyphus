package com.bybutter.sisyphus.middleware.configuration

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.FileUrlResource
import java.net.JarURLConnection
import java.net.URL
import java.util.Properties
import kotlin.io.path.Path

abstract class ConfigFormatFilePropertyExporter : FileConfigPropertyExporter() {
    abstract val names: Collection<String>

    open val formats: Collection<String> = listOf("properties", "yaml", "yml")

    override fun getFiles(environment: String): Iterable<String> {
        return mutableListOf<String>().apply {
            for (name in names) {
                for (format in formats) {
                    add("$name.$format")
                }
                if (environment.isNotEmpty()) {
                    for (format in formats) {
                        add("$name-$environment.$format")
                    }
                }
            }
        }
    }

    override fun read(url: URL): PropertySource<*>? {
        when (val connection = url.openConnection()) {
            is JarURLConnection -> {
                ConfigArtifactProvider.logger.info(
                    "Load config '${connection.entryName}' from artifact '${Path(connection.jarFile.name).fileName}'",
                )
            }
            else -> {
                ConfigArtifactProvider.logger.info("Load config from $url")
            }
        }
        return when {
            url.path.endsWith(".properties") -> readFromProperties(url)
            url.path.endsWith(".yaml") || url.path.endsWith(".yml") -> readFromYaml(url)
            else -> null
        }
    }

    protected fun readFromProperties(url: URL): PropertySource<*>? {
        return url.openStream().use {
            val result = Properties()
            result.load(it)
            PropertiesPropertySource("config:$url", result)
        }
    }

    protected fun readFromYaml(url: URL): PropertySource<*>? {
        val yamlFactory = YamlPropertiesFactoryBean()
        yamlFactory.setResources(FileUrlResource(url))
        yamlFactory.afterPropertiesSet()
        val yamlProperties = yamlFactory.`object` ?: return null
        return PropertiesPropertySource("config:$url", yamlProperties)
    }
}
