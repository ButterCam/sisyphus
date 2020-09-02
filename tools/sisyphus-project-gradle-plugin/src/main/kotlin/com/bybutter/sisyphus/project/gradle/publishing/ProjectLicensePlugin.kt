package com.bybutter.sisyphus.project.gradle.publishing

import com.bybutter.sisyphus.project.gradle.ensurePlugin
import java.io.File
import nebula.plugin.info.scm.ScmInfoExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class ProjectLicensePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("nebula.maven-base-publish", "nebula.info-scm") {
            apply(it)
        }.also {
            if (!it) return
        }

        val file = detectLicenseFile(target) ?: return
        val license = detectLicense(file) ?: return

        val scmInfo = target.extensions.getByType(ScmInfoExtension::class.java)
        val repo = detectGithubRepo(scmInfo.origin) ?: return
        val branch = scmInfo.branch ?: return

        val publishing = target.extensions.getByType(PublishingExtension::class.java)
        publishing.publications.withType(MavenPublication::class.java) {
            it.pom {
                it.licenses {
                    it.license {
                        it.name.set(license)
                        it.url.set("https://github.com/$repo/blob/$branch/${file.name}")
                        it.distribution.set("repo")
                    }
                }
            }
        }
    }

    private fun detectLicenseFile(target: Project): File? {
        for (file in target.rootProject.projectDir.listFiles() ?: arrayOf()) {
            if (file.name.contains("license", true)) {
                return file
            }
        }
        return null
    }

    private fun detectLicense(file: File): String? {
        val license = file.readText()

        for ((name, regex) in licenseRegex) {
            if (regex.containsMatchIn(license)) {
                return name
            }
        }
        return null
    }

    private fun detectGithubRepo(origin: String?): String? {
        origin ?: return null

        for (githubPattern in githubPatterns) {
            val result = githubPattern.matchEntire(origin) ?: continue
            return "${result.groups[1]?.value}/${result.groups[2]?.value}"
        }
        return null
    }

    companion object {
        private val licenseRegex = mapOf(
            "MIT License" to "MIT License".toRegex(),
            "Apache License 2.0" to """Apache License\s+Version 2\.0""".toRegex(),
            "Mozilla Public License 2.0" to """Mozilla Public License Version 2\.0""".toRegex(),
            "GNU AGPLv3" to """GNU AFFERO GENERAL PUBLIC LICENSE\s+Version 3""".toRegex(),
            "GNU GPLv3" to """GNU GENERAL PUBLIC LICENSE\s+Version 3""".toRegex(),
            "GNU LGPLv3" to """GNU LESSER GENERAL PUBLIC LICENSE\s+Version 3""".toRegex(),
            "Boost Software License 1.0" to """Boost Software License - Version 1\.0""".toRegex(),
            "The Unlicense" to """This is free and unencumbered software released into the public domain\.""".toRegex(),
            "WTFPL" to "DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE".toRegex()
        )
        private val githubPatterns = listOf(
            """git@github.com:([a-zA-Z0-9-_.]+?):([a-zA-Z0-9-_.]+?).git""".toRegex(),
            """https?://github.com/([a-zA-Z0-9-_.]+?)/([a-zA-Z0-9-_.]+?)\.git""".toRegex(),
            """https?://github.com/([a-zA-Z0-9-_.]+?)/([a-zA-Z0-9-_.]+)""".toRegex()
        )
    }
}
