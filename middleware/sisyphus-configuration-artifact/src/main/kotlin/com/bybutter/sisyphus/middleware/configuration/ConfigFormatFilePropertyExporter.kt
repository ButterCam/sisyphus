package com.bybutter.sisyphus.middleware.configuration

import java.net.URL
import java.util.Properties
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.FileUrlResource

abstract class ConfigFormatFilePropertyExporter : FileConfigPropertyExporter() {
    abstract val names: Collection<String>

    open val formats: Collection<String> = listOf("properties", "yaml", "yml")

    final override val files: Collection<String> by lazy {
        names.flatMap { name ->
            formats.map { format ->
                "$name.$format"
            }
        }
    }

    override fun read(url: URL): PropertySource<*>? {
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
