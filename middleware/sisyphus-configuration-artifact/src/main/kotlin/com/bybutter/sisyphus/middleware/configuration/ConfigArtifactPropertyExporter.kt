package com.bybutter.sisyphus.middleware.configuration

import org.springframework.core.Ordered
import org.springframework.core.env.PropertySource

interface ConfigArtifactPropertyExporter : Ordered {
    fun export(classLoader: ClassLoader): List<PropertySource<*>>
}
