package com.bybutter.sisyphus.project.gradle.java

import com.bybutter.sisyphus.project.gradle.SisyphusExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaDependenciesReplacePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val sisyphusManagedDependencies = target.extensions.getByType(SisyphusExtension::class.java).managedDependencies
        target.configurations.all {
            it.resolutionStrategy.eachDependency { detail ->
                sisyphusManagedDependencies[detail.requested.group]?.let { moduleStringNotation ->
                    detail.useVersion(moduleStringNotation.version)
                    detail.because("This dependencies version set by Sisyphus")
                }
            }
        }
    }
}
