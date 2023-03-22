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
    implementation(libs.gradle.plugin.publish)
}

gradlePlugin {
    plugins {
        create("sisyphus-java") {
            id = "sisyphus.java"
            implementationClass = "JavaProjectPlugin"
        }
        create("sisyphus-kotlin") {
            id = "sisyphus.kotlin"
            implementationClass = "KotlinProjectPlugin"
        }
        create("sisyphus-library") {
            id = "sisyphus.library"
            implementationClass = "LibraryProjectPlugin"
        }
        create("sisyphus-middleware") {
            id = "sisyphus.middleware"
            implementationClass = "MiddlewareProjectPlugin"
        }
        create("sisyphus-starter") {
            id = "sisyphus.starter"
            implementationClass = "StarterProjectPlugin"
        }
        create("sisyphus-tools") {
            id = "sisyphus.tools"
            implementationClass = "ToolProjectPlugin"
        }
        create("sisyphus-plugin") {
            id = "sisyphus.plugin"
            implementationClass = "GradlePluginProjectPlugin"
        }
    }
}
