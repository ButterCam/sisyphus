package com.bybutter.sisyphus.project.gradle.threepart

import com.bybutter.sisyphus.project.gradle.ensurePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class SisyphusAntlrKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("org.gradle.antlr", "kotlin") {
            apply(it)
        }.also {
            if (!it) return
        }

        val sourceSets = target.extensions.getByType(SourceSetContainer::class.java)
        for (sourceSet in sourceSets) {
            val kotlinTask = target.tasks.findByName(sourceSet.getCompileTaskName("kotlin")) ?: continue
            val antlrTask = target.tasks.findByName(sourceSet.getTaskName("generate", "GrammarSource")) ?: continue
            kotlinTask.dependsOn(antlrTask)
        }
    }
}
