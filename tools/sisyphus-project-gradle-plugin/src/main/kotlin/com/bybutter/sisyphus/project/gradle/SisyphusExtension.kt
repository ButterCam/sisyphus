package com.bybutter.sisyphus.project.gradle

import org.gradle.api.Project

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

    var dependencyRepositories: MutableList<String> = mutableListOf("local", "central", "jcenter", "portal")

    var releaseRepositories: MutableList<String> = mutableListOf("release")

    var snapshotRepositories: MutableList<String> = mutableListOf("snapshot")

    var dockerPublishRegistries: MutableList<String> = mutableListOf()

    val signKeyName: String?

    init {
        val developer: String? = project.findProperty("sisyphus.developer") as? String
        isDevelop = developer != null
        val branchName: String? = System.getenv("BRANCH_NAME")
        val tagName: String? = System.getenv("TAG_NAME")

        version = when {
            developer != null -> "$developer-SNAPSHOT"
            branchName != null -> "$branchName-SNAPSHOT"
            tagName != null -> "$tagName"
            else -> project.version as String
        }

        for (key in project.properties.keys) {
            val result = repositoryUrlRegex.matchEntire(key) ?: continue
            val repositoryName = result.groupValues[1]

            val url = project.findProperty("sisyphus.repositories.$repositoryName.url") as? String ?: continue
            val username = project.findProperty("sisyphus.repositories.$repositoryName.username") as? String ?: continue
            val password = project.findProperty("sisyphus.repositories.$repositoryName.password") as? String ?: continue

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

        signKeyName = project.findProperty("signing.gnupg.keyName") as? String
    }

    companion object {
        private val repositoryUrlRegex = """sisyphus\.repositories\.([A-Za-z][A-Za-z0-9-_]+)\.url""".toRegex()
    }
}
