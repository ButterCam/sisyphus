tools

plugins {
    `java-library`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.1"
}

dependencies {
    implementation(Dependencies.Kotlin.reflect)
    implementation(Dependencies.Kotlin.plugin)

    compileOnly("com.netflix.nebula:nebula-publishing-plugin:17.2.1")
    compileOnly("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
}

gradlePlugin {
    plugins {
        create("sisyphus") {
            id = "com.bybutter.sisyphus.project"
            displayName = "Plugin for developing project based on sisyphus framework."
            description = "Easy configuare develop environment for project based on sisyphus framework."
            implementationClass = "com.bybutter.sisyphus.project.gradle.SisyphusProjectPlugin"
        }
    }
}
