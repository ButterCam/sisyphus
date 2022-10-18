package com.bybutter.sisyphus.project.gradle.publishing

import com.bybutter.sisyphus.project.gradle.SisyphusExtension
import com.bybutter.sisyphus.project.gradle.applyFromRepositoryKeys
import com.bybutter.sisyphus.project.gradle.ensurePlugin
import com.bybutter.sisyphus.project.gradle.isRelease
import com.bybutter.sisyphus.project.gradle.isSnapshot
import com.bybutter.sisyphus.project.gradle.tryApplyPluginClass
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

class ProjectPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("nebula.maven-base-publish") {
            apply(it)
        }.also {
            if (!it) return
        }

        target.tryApplyPluginClass("nebula.plugin.info.InfoPlugin")

        val sisyphus = target.extensions.getByType(SisyphusExtension::class.java)
        val publishing = target.extensions.getByType(PublishingExtension::class.java)

        if (target.isRelease()) {
            publishing.repositories.applyFromRepositoryKeys(sisyphus.repositories.get(), sisyphus.releaseRepositories.get())
        }
        if (target.isSnapshot()) {
            publishing.repositories.applyFromRepositoryKeys(sisyphus.repositories.get(), sisyphus.snapshotRepositories.get())
        }
    }
}
