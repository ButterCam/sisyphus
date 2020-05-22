package com.bybutter.sisyphus.project.gradle

import java.net.URI
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler

internal inline fun Project.ensurePlugin(id: String, noinline block: (Project) -> Unit, returnBlock: () -> Unit) {
    if (!pluginManager.hasPlugin(id)) {
        pluginManager.withPlugin(id) {
            block(this)
        }
        returnBlock()
    }
}

internal fun Project.tryApplyPluginClass(className: String, action: () -> Unit = {}): Boolean {
    return try {
        val plugin = Class.forName(className)
        action()
        this.pluginManager.apply(plugin)
        true
    } catch (ex: ClassNotFoundException) {
        false
    }
}

internal fun RepositoryHandler.applyFromRepositoryKeys(repositories: Map<String, Repository>, repositoryKeys: Collection<String>) {
    for (repositoryKey in repositoryKeys) {
        val repository = when (repositoryKey) {
            "local" -> repositories[repositoryKey] ?: run {
                this.mavenLocal()
                null
            }
            "central" -> repositories[repositoryKey] ?: run {
                this.mavenCentral()
                null
            }
            "jcenter" -> repositories[repositoryKey] ?: run {
                this.jcenter()
                null
            }
            "portal" -> repositories[repositoryKey] ?: run {
                this.gradlePluginPortal()
                null
            }
            else -> repositories[repositoryKey]
        }

        repository ?: continue

        this.maven {
            it.name = repositoryKey
            it.url = URI.create(repository.url)
            it.credentials.username = repository.username
            it.credentials.password = repository.password
        }
    }
}
