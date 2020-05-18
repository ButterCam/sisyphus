package com.bybutter.sisyphus.project.gradle

import java.io.File
import java.net.URI
import nebula.plugin.publishing.maven.MavenPublishPlugin
import nebula.plugin.publishing.publications.SourceJarPlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

class SisyphusProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        applyBase(target)
        tryApplyJavaBase(target)
        tryApplyLibrary(target)
        tryApplyPlatform(target)
        tryApplyPublish(target)
        tryApplyKtlint(target)
        tryApplyAntlr(target)
    }

    private fun applyBase(target: Project) {
        val extension = target.extensions.create("sisyphus", SisyphusExtension::class.java, target)
        target.version = extension.version
    }

    private fun tryApplyJavaBase(target: Project) {
        if (!target.pluginManager.hasPlugin("java-base")) {
            target.pluginManager.withPlugin("java-base") {
                tryApplyJavaBase(target)
            }
            return
        }

        val extension = target.extensions.getByType(SisyphusExtension::class.java)
        target.repositories.applyFromRepositoryKeys(extension.repositories, extension.dependencyRepositories)
    }

    private fun tryApplyLibrary(target: Project) {
        if (!target.pluginManager.hasPlugin("java-library")) {
            target.pluginManager.withPlugin("java-library") {
                tryApplyLibrary(target)
            }
            return
        }

        try {
            target.pluginManager.apply(MavenPublishPlugin::class.java)
            target.pluginManager.apply(SourceJarPlugin::class.java)
        } catch (exception: NoClassDefFoundError) {
            target.logger.debug("Skip apply library plugin due to java library plugins not existed.")
            return
        }
    }

    private fun tryApplyPlatform(target: Project) {
        if (!target.pluginManager.hasPlugin("java-platform")) {
            target.pluginManager.withPlugin("java-platform") {
                tryApplyPlatform(target)
            }
            return
        }

        try {
            target.pluginManager.apply(MavenPublishPlugin::class.java)
        } catch (exception: NoClassDefFoundError) {
            target.logger.debug("Skip apply platform plugin due to java platform plugins not existed.")
            return
        }

        target.afterEvaluate {
            val publishing = it.extensions.getByType(PublishingExtension::class.java)
            publishing.publications.withType(MavenPublication::class.java) {
                it.from(target.components.getByName("javaPlatform"))
            }
        }
    }

    private fun tryApplyPublish(target: Project) {
        if (!target.pluginManager.hasPlugin("nebula.maven-base-publish")) {
            target.pluginManager.withPlugin("nebula.maven-base-publish") {
                tryApplyPublish(target)
            }
            return
        }

        val extension = target.extensions.getByType(SisyphusExtension::class.java)
        val publishingExtension = target.extensions.getByType(PublishingExtension::class.java)

        if(!extension.signKeyName.isNullOrEmpty()) {
            target.pluginManager.apply(SigningPlugin::class.java)
            val signing = target.extensions.getByType(SigningExtension::class.java)
            signing.useGpgCmd()
            target.afterEvaluate {
                publishingExtension.publications.all {
                    signing.sign(it)
                }
            }
        }

        if (extension.isRelease) {
            publishingExtension.repositories.applyFromRepositoryKeys(extension.repositories, extension.releaseRepositories)
        }
        if (extension.isSnapshot) {
            publishingExtension.repositories.applyFromRepositoryKeys(extension.repositories, extension.snapshotRepositories)
        }
    }

    private fun tryApplyKtlint(target: Project) {
        if (!target.pluginManager.hasPlugin("org.jlleitschuh.gradle.ktlint")) {
            target.pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
                tryApplyKtlint(target)
            }
            return
        }

        val extension = target.extensions.getByType(KtlintExtension::class.java)
        extension.filter(Action {
            val pattern = "${File.separatorChar}generated${File.separatorChar}"
            it.exclude {
                it.file.path.contains(pattern)
            }
        })
        extension.reporters(Action<KtlintExtension.ReporterExtension> {
            it.reporter(ReporterType.CHECKSTYLE)
        })
    }

    private fun tryApplyAntlr(target: Project) {
        if (!target.pluginManager.hasPlugin("org.gradle.antlr")) {
            target.pluginManager.withPlugin("org.gradle.antlr") {
                tryApplyAntlr(target)
            }
            return
        }

        if (!target.pluginManager.hasPlugin("kotlin")) {
            target.pluginManager.withPlugin("kotlin") {
                tryApplyAntlr(target)
            }
            return
        }

        val sourceSets = target.extensions.getByType(SourceSetContainer::class.java)
        for (sourceSet in sourceSets) {
            val kotlinTask = target.tasks.findByName(sourceSet.getCompileTaskName("kotlin")) ?: continue
            val antlrTask = target.tasks.findByName(sourceSet.getTaskName("generate", "GrammarSource")) ?: continue
            kotlinTask.dependsOn(antlrTask)
        }
    }

    private fun RepositoryHandler.applyFromRepositoryKeys(repositories: Map<String, Repository>, repositoryKeys: Collection<String>) {
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
}
