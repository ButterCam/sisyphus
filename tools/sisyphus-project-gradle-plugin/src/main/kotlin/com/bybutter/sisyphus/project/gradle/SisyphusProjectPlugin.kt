package com.bybutter.sisyphus.project.gradle

import com.bybutter.sisyphus.project.gradle.java.JavaBaseProjectPlugin
import com.bybutter.sisyphus.project.gradle.java.JavaLibraryProjectPlugin
import com.bybutter.sisyphus.project.gradle.java.JavaPlatformProjectPlugin
import com.bybutter.sisyphus.project.gradle.publishing.ProjectContactsPlugin
import com.bybutter.sisyphus.project.gradle.publishing.ProjectLicensePlugin
import com.bybutter.sisyphus.project.gradle.publishing.ProjectPublishingPlugin
import com.bybutter.sisyphus.project.gradle.publishing.ProjectSigningPlugin
import com.bybutter.sisyphus.project.gradle.threepart.SisyphusAntlrKotlinPlugin
import com.bybutter.sisyphus.project.gradle.threepart.SisyphusDockerPlugin
import com.bybutter.sisyphus.project.gradle.threepart.SisyphusKtlintPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class SisyphusProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        applyBase(target)

        target.pluginManager.apply(JavaBaseProjectPlugin::class.java)
        target.pluginManager.apply(JavaLibraryProjectPlugin::class.java)
        target.pluginManager.apply(JavaPlatformProjectPlugin::class.java)
        target.pluginManager.apply(ProjectPublishingPlugin::class.java)
        target.pluginManager.apply(ProjectContactsPlugin::class.java)
        target.pluginManager.apply(ProjectLicensePlugin::class.java)
        target.pluginManager.apply(ProjectSigningPlugin::class.java)
        target.pluginManager.apply(SisyphusAntlrKotlinPlugin::class.java)
        if (isClassExist("com.bmuschko.gradle.docker.DockerRemoteApiPlugin")) {
            target.pluginManager.apply(SisyphusDockerPlugin::class.java)
        }
        if (isClassExist("org.jlleitschuh.gradle.ktlint.KtlintExtension")) {
            target.pluginManager.apply(SisyphusKtlintPlugin::class.java)
        }
    }

    private fun applyBase(target: Project) {
        val extension = target.extensions.create("sisyphus", SisyphusExtension::class.java, target)
        if (target.version.toString() == Project.DEFAULT_VERSION) {
            extension.recommendVersion()?.let {
                target.version = it
            }
        }
    }
}
