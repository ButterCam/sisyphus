package com.bybutter.sisyphus.project.gradle.java

import com.bybutter.sisyphus.project.gradle.SisyphusExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySubstitutions
import org.gradle.api.artifacts.component.ComponentSelector
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.attributes.Category
import org.gradle.kotlin.dsl.SisyphusDevelopmentLayer

class JavaDependenciesReplacePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.getByType(SisyphusExtension::class.java)

        target.configurations.all {
            it.resolutionStrategy.eachDependency { detail ->
                extension.managedDependencies["${detail.requested.group}:${detail.requested.name}"]?.let { moduleStringNotation ->
                    detail.useVersion(moduleStringNotation.version)
                    detail.because("The version of current dependency managed by Sisyphus Property")
                }
            }
            it.resolutionStrategy.dependencySubstitution { sub ->
                sub.all dep@{
                    val dependency = it.requested
                    val layer = dependency.attributes.getAttribute(SisyphusDevelopmentLayer.attribute)?.ordinal
                        ?: return@dep

                    if (extension.layer.ordinal >= layer) {
                        if (dependency is ModuleComponentSelector) {
                            it.useTarget(
                                sub.replaceVersion(dependency, target.version) ?: return@dep,
                                "The version of current dependency managed by Sisyphus Layer"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun DependencySubstitutions.replaceVersion(
        dependency: ModuleComponentSelector,
        version: Any
    ): ComponentSelector? {
        return when (dependency.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name) {
            Category.LIBRARY -> module("${dependency.moduleIdentifier}:$version")
            Category.REGULAR_PLATFORM, Category.ENFORCED_PLATFORM -> platform(module("${dependency.moduleIdentifier}:$version"))
            else -> null
        }
    }
}
