package com.bybutter.sisyphus.project.gradle.java

import com.bybutter.sisyphus.project.gradle.SisyphusExtension
import com.bybutter.sisyphus.project.gradle.applyFromRepositoryKeys
import com.bybutter.sisyphus.project.gradle.ensurePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaBaseProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("java-base", ::apply) {
            return
        }

        val extension = target.extensions.getByType(SisyphusExtension::class.java)
        target.repositories.applyFromRepositoryKeys(extension.repositories, extension.dependencyRepositories)
    }
}