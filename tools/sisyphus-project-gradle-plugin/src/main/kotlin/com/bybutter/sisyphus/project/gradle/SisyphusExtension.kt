package com.bybutter.sisyphus.project.gradle

import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dsl.ParsedModuleStringNotation
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.SisyphusDevelopmentLayer

open class SisyphusExtension(val project: Project) {
    val developer: Property<String> = project.objects.property(String::class.java)

    val layer: Property<SisyphusDevelopmentLayer> =
        project.objects.property(SisyphusDevelopmentLayer::class.java).value(SisyphusDevelopmentLayer.IMPLEMENTATION)

    val repositories: MapProperty<String, Repository> =
        project.objects.mapProperty(String::class.java, Repository::class.java).empty()

    val dependencyRepositories: ListProperty<String> =
        project.objects.listProperty(String::class.java).value(listOf("local", "central", "portal", "google"))

    val releaseRepositories: ListProperty<String> =
        project.objects.listProperty(String::class.java).value(listOf("release"))

    val snapshotRepositories: ListProperty<String> =
        project.objects.listProperty(String::class.java).value(listOf("snapshot"))

    val dockerPublishRegistries: ListProperty<String> = project.objects.listProperty(String::class.java).empty()

    val managedDependencies: MapProperty<String, ParsedModuleStringNotation> =
        project.objects.mapProperty(String::class.java, ParsedModuleStringNotation::class.java).empty()

    init {
        developer.set(project.findProperty("sisyphus.developer") as? String)
        for (key in project.properties.keys) {
            val result = repositoryUrlRegex.matchEntire(key) ?: continue
            val repositoryName = result.groupValues[1]

            val url = project.findProperty("sisyphus.repositories.$repositoryName.url") as? String ?: continue
            val username = project.findProperty("sisyphus.repositories.$repositoryName.username") as? String
            val password = project.findProperty("sisyphus.repositories.$repositoryName.password") as? String

            repositories.put(repositoryName, Repository(url, username, password))
        }

        (project.findProperty("sisyphus.dependency.repositories") as? String)?.split(',')?.let {
            dependencyRepositories.set(it)
        }
        (project.findProperty("sisyphus.release.repositories") as? String)?.split(',')?.let {
            releaseRepositories.set(it)
        }
        (project.findProperty("sisyphus.snapshot.repositories") as? String)?.split(',')?.let {
            snapshotRepositories.set(it)
        }
        (project.findProperty("sisyphus.docker.repositories") as? String)?.split(',')?.let {
            dockerPublishRegistries.set(it)
        }
        (project.findProperty("sisyphus.dependency.overriding") as? String)?.split(',')?.associate {
            val moduleStringNotation = ParsedModuleStringNotation(it, "")
            "${moduleStringNotation.group}:${moduleStringNotation.name}" to moduleStringNotation
        }?.let {
            managedDependencies.set(it)
        }
        (project.findProperty("sisyphus.layer") as? String)?.let { SisyphusDevelopmentLayer.valueOf(it) }?.let {
            layer.set(it)
        }
    }

    fun recommendVersion(): String? {
        val branchName: String? = System.getenv("BRANCH_NAME")
        val githubRef: String? = System.getenv("GITHUB_REF")
        val tagName: String? = System.getenv("TAG_NAME")
        val buildVersion: String? = System.getenv("BUILD_VERSION")

        return when {
            !developer.orNull.isNullOrEmpty() -> "${developer.get()}-SNAPSHOT"
            buildVersion != null -> "$buildVersion"
            tagName != null -> "$tagName"
            branchName != null -> "$branchName-SNAPSHOT"
            githubRef != null && pullRequestRefRegex.matches(githubRef) ->
                "PR-${
                pullRequestRefRegex.matchEntire(
                    githubRef
                )?.groupValues?.get(1)
                }-SNAPSHOT"

            else -> null
        }
    }

    companion object {
        private val repositoryUrlRegex = """sisyphus\.repositories\.([A-Za-z][A-Za-z0-9-_]+)\.url""".toRegex()
        private val pullRequestRefRegex = """refs/pull/([0-9]+)/merge""".toRegex()
    }
}
