package com.bybutter.sisyphus.middleware.configuration

import java.net.URL
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.FileUrlResource

abstract class YamlConfigPropertyExporter : FileConfigPropertyExporter() {
    override fun read(url: URL): PropertySource<*>? {
        val yamlFactory = YamlPropertiesFactoryBean()
        yamlFactory.setResources(FileUrlResource(url))
        yamlFactory.afterPropertiesSet()
        val yamlProperties = yamlFactory.`object` ?: return null
        return PropertiesPropertySource("config:${url.hashCode()}", yamlProperties)
    }
}
