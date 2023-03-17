plugin

plugins {
    `java-library`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

description = "Plugin for easy deploying and debugging docker image on Kubernetes cluster"

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.gradle.kotlin)
    implementation(libs.kubernetes)
}

gradlePlugin {
    website.set("https://sisyphus.bybutter.com")
    vcsUrl.set("https://github.com/ButterCam/sisyphus")

    plugins {
        create("sisyphus-k8s") {
            id = "com.bybutter.sisyphus.k8s"
            displayName = "Sisyphus k8s Plugin"
            description = "Deploy and debug docker image on Kubernetes cluster."
            implementationClass = "com.bybutter.sisyphus.k8s.gradle.SisyphusK8sPlugin"
            tags.set(listOf("sisyphus", "deploy", "k8s", "kubernetes"))
        }
    }
}
