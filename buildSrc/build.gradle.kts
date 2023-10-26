import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `java-library`
    `java-gradle-plugin`
    `kotlin-dsl`
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
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_17.majorVersion
    targetCompatibility = JavaVersion.VERSION_17.majorVersion
}
