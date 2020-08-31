package com.bybutter.sisyphus.project.gradle.java

import com.bybutter.sisyphus.project.gradle.ensurePlugin
import com.bybutter.sisyphus.project.gradle.tryApplyPluginClass
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaLibraryProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("java-library") {
            apply(it)
        }.also {
            if (!it) return
        }

        target.tryApplyPluginClass("nebula.plugin.publishing.maven.MavenPublishPlugin")
        target.tryApplyPluginClass("nebula.plugin.publishing.publications.JavadocJarPlugin")
        target.tryApplyPluginClass("nebula.plugin.publishing.publications.SourceJarPlugin")
    }
}
