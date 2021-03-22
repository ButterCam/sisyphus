plugins {
    `java-library`
    `kotlin-dsl`
    id("idea")
    id("com.bybutter.sisyphus.project") version "1.1.4"
}

dependencies {
    implementation(platform("com.bybutter.sisyphus:sisyphus-dependencies:1.1.4"))
    implementation("com.bybutter.sisyphus.tools:sisyphus-protobuf-gradle-plugin")
    implementation("com.bybutter.sisyphus.tools:sisyphus-project-gradle-plugin")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("org.jetbrains.kotlin:kotlin-allopen")
    implementation("org.springframework.boot:spring-boot-gradle-plugin")
    implementation("org.jlleitschuh.gradle:ktlint-gradle")
    implementation("com.github.ben-manes:gradle-versions-plugin")
    implementation("com.netflix.nebula:nebula-publishing-plugin")
    implementation("com.netflix.nebula:gradle-contacts-plugin")
    implementation("com.netflix.nebula:gradle-info-plugin")
    implementation("org.gradle.kotlin:plugins")
    implementation("com.gradle.publish:plugin-publish-plugin")
    implementation("org.eclipse.jgit:org.eclipse.jgit")
}
