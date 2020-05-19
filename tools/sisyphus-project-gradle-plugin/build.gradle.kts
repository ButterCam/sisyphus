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

    compileOnly("com.netflix.nebula:nebula-publishing-plugin:17.2.1")
    compileOnly("com.netflix.nebula:gradle-info-plugin:7.1.4")
    compileOnly("com.netflix.nebula:gradle-contacts-plugin:5.1.0")
    compileOnly("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
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
