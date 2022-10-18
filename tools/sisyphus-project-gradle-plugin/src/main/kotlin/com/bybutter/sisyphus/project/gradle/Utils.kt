package com.bybutter.sisyphus.project.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

internal fun Project.isSnapshot(): Boolean {
    return version.toString().endsWith("-SNAPSHOT")
}

internal fun Project.isRelease(): Boolean {
    return !isSnapshot()
}

internal fun Project.ensurePlugin(vararg ids: String, block: (Project) -> Unit): Boolean {
    for (id in ids) {
        if (!pluginManager.hasPlugin(id)) {
            pluginManager.withPlugin(id) {
                block(this)
            }
            return false
        }
    }

    return true
}

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

internal fun RepositoryHandler.applyFromRepositoryKeys(
    repositories: Map<String, Repository>,
    repositoryKeys: Collection<String>
) {
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

            "portal" -> repositories[repositoryKey] ?: run {
                this.gradlePluginPortal()
                null
            }

            "google" -> repositories[repositoryKey] ?: run {
                this.google()
                null
            }

            else -> repositories[repositoryKey]
        }

        repository ?: continue

        this.maven {
            it.name = repositoryKey
            it.url = URI.create(repository.url)
            if (repository.username != null) {
                it.credentials.username = repository.username
            }
            if (repository.password != null) {
                it.credentials.password = repository.password
            }
        }
    }
}

internal fun isClassExist(className: String): Boolean {
    return try {
        Class.forName(className)
        true
    } catch (ex: ClassNotFoundException) {
        false
    }
}

fun getJavaMajorVersion(): Int? {
    val javaVersion = System.getProperty("java.version")
    return if (javaVersion.startsWith("1.")) {
        javaVersion.substring(2, 3)
    } else {
        javaVersion.substringBefore('.')
    }.toIntOrNull()
}
