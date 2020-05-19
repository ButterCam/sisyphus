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
    implementation("com.netflix.nebula:gradle-contacts-plugin:5.1.0")
    implementation("com.netflix.nebula:gradle-info-plugin:7.1.4")
    implementation("org.gradle.kotlin:plugins:1.2.11")
    implementation("com.gradle.publish:plugin-publish-plugin:0.11.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.7.0.202003110725-r")
}