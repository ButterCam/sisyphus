package com.bybutter.sisyphus.middleware.configuration

import java.net.URL
import org.springframework.core.Ordered
import org.springframework.core.env.PropertySource

abstract class FileConfigPropertyExporter : ConfigArtifactPropertyExporter {
    protected abstract val files: Collection<String>

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }

    protected abstract fun read(url: URL): PropertySource<*>?

    override fun export(classLoader: ClassLoader): List<PropertySource<*>> {
        return files.asSequence().flatMap {
            classLoader.getResources(it).asSequence()
        }.mapNotNull { read(it) }.toList()
    }
}
