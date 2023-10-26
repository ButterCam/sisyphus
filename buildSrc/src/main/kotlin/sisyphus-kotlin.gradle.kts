import org.gradle.api.JavaVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("sisyphus-java")
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.jmailen.kotlinter")
}

dependencies {
    add("api", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}


tasks.withType<LintTask> {
    val buildPath = layout.buildDirectory.asFile.get().path
    exclude {
        it.file.path.startsWith(buildPath)
    }
}

tasks.withType<FormatTask> {
    val buildPath = layout.buildDirectory.asFile.get().path
    exclude {
        it.file.path.startsWith(buildPath)
    }
}