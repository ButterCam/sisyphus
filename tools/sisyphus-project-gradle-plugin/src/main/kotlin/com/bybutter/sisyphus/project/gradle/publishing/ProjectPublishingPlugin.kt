package com.bybutter.sisyphus.project.gradle.publishing

import com.bybutter.sisyphus.project.gradle.SisyphusExtension
import com.bybutter.sisyphus.project.gradle.applyFromRepositoryKeys
import com.bybutter.sisyphus.project.gradle.ensurePlugin
import com.bybutter.sisyphus.project.gradle.tryApplyPluginClass
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

class ProjectPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("nebula.maven-base-publish", ::apply) {
            return
        }

        target.tryApplyPluginClass("nebula.plugin.info.InfoPlugin")

        val sisyphus = target.extensions.getByType(SisyphusExtension::class.java)
        val publishing = target.extensions.getByType(PublishingExtension::class.java)

        if (sisyphus.isRelease) {
            publishing.repositories.applyFromRepositoryKeys(sisyphus.repositories, sisyphus.releaseRepositories)
        }
        if (sisyphus.isSnapshot) {
            publishing.repositories.applyFromRepositoryKeys(sisyphus.repositories, sisyphus.snapshotRepositories)
        }
    }
}
