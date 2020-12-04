package com.bybutter.sisyphus.middleware.configuration

import java.net.URL
import org.springframework.core.Ordered
import org.springframework.core.env.PropertySource

abstract class FileConfigPropertyExporter : ConfigArtifactPropertyExporter {
    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }

    protected abstract fun getFiles(environment: String): Iterable<String>

    protected abstract fun read(url: URL): PropertySource<*>?

    override fun export(environment: String, classLoader: ClassLoader): List<PropertySource<*>> {
        return getFiles(environment).asSequence().flatMap {
            classLoader.getResources(it).asSequence()
        }.mapNotNull { read(it) }.toList()
    }
}
