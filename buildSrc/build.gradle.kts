plugins {
    `java-library`
    `kotlin-dsl`
    id("idea")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.nebula.contacts)
    implementation(libs.nebula.info)
    implementation(libs.nebula.publishing)
    implementation(libs.gradle.sisyphus)
    implementation(libs.gradle.sisyphus.protobuf)
    implementation(libs.gradle.ktlint)
    implementation(libs.gradle.spring)
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.kotlin.allopen)
    implementation(libs.gradle.plugin.publish)
}
