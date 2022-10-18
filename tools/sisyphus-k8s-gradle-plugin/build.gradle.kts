tools

plugins {
    `java-library`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

description = "Plugin for easy deploying and debugging docker image on Kubernetes cluster"

dependencies {
    implementation(Dependencies.Kotlin.reflect)
    implementation(Dependencies.Kotlin.plugin)
    implementation(Dependencies.kubeJavaClient)
}

gradlePlugin {
    plugins {
        create("sisyphus-k8s") {
            id = "com.bybutter.sisyphus.k8s"
            displayName = "Sisyphus k8s Plugin"
            description = "Deploy and debug docker image on Kubernetes cluster."
            implementationClass = "com.bybutter.sisyphus.k8s.gradle.SisyphusK8sPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/ButterCam/sisyphus"
    vcsUrl = "https://github.com/ButterCam/sisyphus"
    description = "Easy configure develop environment for project based on sisyphus framework."

    (plugins) {
        "sisyphus-k8s" {
            tags = listOf("sisyphus", "deploy", "k8s", "kubernetes")
        }
    }
}
