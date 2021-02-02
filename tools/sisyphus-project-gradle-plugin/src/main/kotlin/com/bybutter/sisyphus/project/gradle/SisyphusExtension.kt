package com.bybutter.sisyphus.project.gradle

import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dsl.ParsedModuleStringNotation

open class SisyphusExtension(val project: Project) {
    var version: String

    val isDevelop: Boolean

    val isSnapshot: Boolean
        get() {
            return version.endsWith("-SNAPSHOT")
        }

    val isRelease: Boolean
        get() {
            return !isSnapshot
        }

    var repositories: MutableMap<String, Repository> = hashMapOf()

    var dependencyRepositories: MutableList<String> = mutableListOf("local", "central", "jcenter", "portal", "google")

    var releaseRepositories: MutableList<String> = mutableListOf("release")

    var snapshotRepositories: MutableList<String> = mutableListOf("snapshot")

    var dockerPublishRegistries: MutableList<String> = mutableListOf()

    var managedDependencies: MutableMap<String, ParsedModuleStringNotation> = mutableMapOf()

    val signKeyName: String?

    init {
        val developer: String? = project.findProperty("sisyphus.developer") as? String
        isDevelop = developer != null
        val branchName: String? = System.getenv("BRANCH_NAME")
        val githubRef: String? = System.getenv("GITHUB_REF")
        val tagName: String? = System.getenv("TAG_NAME")
        val buildVersion: String? = System.getenv("BUILD_VERSION")

        version = when {
            developer != null -> "$developer-SNAPSHOT"
            buildVersion != null -> "$buildVersion"
            tagName != null -> "$tagName"
            branchName != null -> "$branchName-SNAPSHOT"
            githubRef != null && pullRequestRefRegex.matches(githubRef) -> "PR-${pullRequestRefRegex.matchEntire(githubRef)?.groupValues?.get(1)}-SNAPSHOT"
            else -> project.version as String
        }

        for (key in project.properties.keys) {
            val result = repositoryUrlRegex.matchEntire(key) ?: continue
            val repositoryName = result.groupValues[1]

            val url = project.findProperty("sisyphus.repositories.$repositoryName.url") as? String ?: continue
            val username = project.findProperty("sisyphus.repositories.$repositoryName.username") as? String
            val password = project.findProperty("sisyphus.repositories.$repositoryName.password") as? String

            repositories[repositoryName] = Repository(url, username, password)
        }

        dependencyRepositories = (project.findProperty("sisyphus.dependency.repositories") as? String)?.split(',')?.toMutableList()
                ?: dependencyRepositories
        releaseRepositories = (project.findProperty("sisyphus.release.repositories") as? String)?.split(',')?.toMutableList()
                ?: releaseRepositories
        snapshotRepositories = (project.findProperty("sisyphus.snapshot.repositories") as? String)?.split(',')?.toMutableList()
                ?: snapshotRepositories
        dockerPublishRegistries = (project.findProperty("sisyphus.docker.repositories") as? String)?.split(',')?.toMutableList()
                ?: dockerPublishRegistries

        managedDependencies = (project.findProperty("sisyphus.dependency.overriding") as? String)?.split(',')?.associate {
            val moduleStringNotation = ParsedModuleStringNotation(it, null)
            "${moduleStringNotation.group}:${moduleStringNotation.name}" to moduleStringNotation
        }?.toMutableMap() ?: managedDependencies

        signKeyName = project.findProperty("signing.gnupg.keyName") as? String
    }

    companion object {
        private val repositoryUrlRegex = """sisyphus\.repositories\.([A-Za-z][A-Za-z0-9-_]+)\.url""".toRegex()
        private val pullRequestRefRegex = """refs/pull/([0-9]+)/merge""".toRegex()
    }
}
