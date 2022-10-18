package com.bybutter.sisyphus.project.gradle.threepart.docker

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

open class ExtractBootJarLayer : Exec() {
    @InputFiles
    lateinit var bootJars: FileCollection

    @OutputDirectory
    lateinit var outputDirectory: File

    init {
        this.executable = Path(System.getProperty("java.home")).resolve("bin/java").absolutePathString()

        this.args("-Djarmode=layertools")
        this.args("-jar")
    }

    override fun exec() {
        this.workingDir = outputDirectory
        this.args(bootJars.firstOrNull { it.name.endsWith(".jar") }?.absolutePath ?: return)
        this.args("extract")
        super.exec()
    }
}
