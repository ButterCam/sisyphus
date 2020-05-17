package com.bybutter.sisyphus.middleware.configuration

import java.net.URL
import java.util.Properties
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource

abstract class PropertiesConfigPropertyExporter : FileConfigPropertyExporter() {
    override fun read(url: URL): PropertySource<*>? {
        return url.openStream().use {
            val result = Properties()
            result.load(it)
            PropertiesPropertySource("config:${url.hashCode()}", result)
        }
    }
}
