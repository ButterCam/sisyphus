package com.bybutter.sisyphus.project.gradle.java

import com.bybutter.sisyphus.project.gradle.SisyphusExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaDependenciesReplacePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.getByType(SisyphusExtension::class.java)

        target.configurations.all {
            it.resolutionStrategy.eachDependency { detail ->
                extension.managedDependencies.getting("${detail.requested.group}:${detail.requested.name}")?.orNull?.let { moduleStringNotation ->
                    detail.useVersion(moduleStringNotation.version)
                    detail.because("The version of current dependency managed by Sisyphus Property")
                }
            }
        }
    }
}
