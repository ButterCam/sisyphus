package com.bybutter.sisyphus.middleware.configuration

import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import java.net.URL
import java.util.Properties

abstract class PropertiesConfigPropertyExporter : FileConfigPropertyExporter() {
    override fun read(url: URL): PropertySource<*>? {
        return url.openStream().use {
            val result = Properties()
            result.load(it)
            PropertiesPropertySource("config:${url.hashCode()}", result)
        }
    }
}
