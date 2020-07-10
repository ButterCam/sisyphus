tools

plugins {
    `java-library`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

description = "Plugin for  checks for compliance with many of Google’s API standards with Gradle in Sisyphus Framework"

dependencies {
    implementation(Dependencies.Spring.Boot.boot)
}
