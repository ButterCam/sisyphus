tools

plugins {
    `java-library`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

description = "Plugin for easy configuring Gradle and plugins in Sisyphus Framework"

dependencies {
    implementation(Dependencies.Kotlin.reflect)
    implementation(Dependencies.Kotlin.plugin)

    compileOnly("com.netflix.nebula:nebula-publishing-plugin")
    compileOnly("com.netflix.nebula:gradle-info-plugin")
    compileOnly("com.netflix.nebula:gradle-contacts-plugin")
    compileOnly("org.jlleitschuh.gradle:ktlint-gradle")
    compileOnly("com.palantir.gradle.docker:gradle-docker")
    compileOnly("org.springframework.boot:spring-boot-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("sisyphus") {
            id = "com.bybutter.sisyphus.project"
            displayName = "Sisyphus Project Plugin"
            description = "Easy configure develop environment for project based on sisyphus framework."
            implementationClass = "com.bybutter.sisyphus.project.gradle.SisyphusProjectPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/ButterCam/sisyphus"
    vcsUrl = "https://github.com/ButterCam/sisyphus"
    description = "Easy configure develop environment for project based on sisyphus framework."

    (plugins) {
        "sisyphus" {
            tags = listOf("sisyphus", "project")
        }
    }
}
