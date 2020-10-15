package com.bybutter.sisyphus.project.gradle.java

import com.bybutter.sisyphus.project.gradle.ensurePlugin
import com.bybutter.sisyphus.project.gradle.tryApplyPluginClass
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class JavaPlatformProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("java-platform") {
            apply(it)
        }.also {
            if (!it) return
        }

        target.tryApplyPluginClass("nebula.plugin.publishing.maven.MavenPublishPlugin")

        target.afterEvaluate {
            val publishing = it.extensions.getByType(PublishingExtension::class.java)
            publishing.publications.withType(MavenPublication::class.java) {
                it.from(target.components.getByName("javaPlatform"))
            }
        }
        target.pluginManager.apply(JavaDependenciesReplacePlugin::class.java)
    }
}
