package com.bybutter.sisyphus.project.gradle.threepart

import com.bybutter.sisyphus.project.gradle.ensurePlugin
import java.io.File
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

class SisyphusKtlintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("org.jlleitschuh.gradle.ktlint") {
            apply(it)
        }.also {
            if (!it) return
        }

        val extension = target.extensions.getByType(KtlintExtension::class.java)
        extension.filter(Action {
            val pattern = "${File.separatorChar}generated${File.separatorChar}"
            it.exclude {
                it.file.path.contains(pattern)
            }
        })
        extension.reporters(Action<KtlintExtension.ReporterExtension> {
            it.reporter(ReporterType.CHECKSTYLE)
        })
    }
}
