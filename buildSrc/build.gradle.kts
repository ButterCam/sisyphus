plugins {
    `java-library`
    `kotlin-dsl`
    id("idea")
    id("com.bybutter.sisyphus.project") version "higan-SNAPSHOT"
}

dependencies {
    implementation(platform("com.bybutter.sisyphus:sisyphus-bom:higan-SNAPSHOT"))
    implementation("com.bybutter.sisyphus.tools:sisyphus-protobuf-gradle-plugin")
    implementation("com.bybutter.sisyphus.tools:sisyphus-project-gradle-plugin")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.70")
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.3.70")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.2.7.RELEASE")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.27.0")
    implementation("com.netflix.nebula:nebula-publishing-plugin:17.2.1")
    implementation("org.gradle.kotlin:plugins:1.2.11")
}