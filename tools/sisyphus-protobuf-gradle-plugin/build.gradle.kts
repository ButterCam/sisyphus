tools

plugins {
    `java-library`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

description = "Plugin for compiling proto files with Gradle in Sisyphus Framework"

dependencies {
    implementation(project(":lib:sisyphus-common"))
    implementation(project(":lib:sisyphus-jackson"))
    implementation(project(":tools:sisyphus-protoc"))
    implementation(project(":tools:sisyphus-api-linter-runner"))

    compileOnly("com.android.tools.build:gradle")

    implementation(Dependencies.Kotlin.reflect)
    implementation(Dependencies.Kotlin.plugin)
    implementation(Dependencies.Proto.apiCompiler)
}

gradlePlugin {
    plugins {
        create("protobuf") {
            id = "com.bybutter.sisyphus.protobuf"
            displayName = "Sisyphus Protobuf Plugin"
            description = "Protobuf compiler plugin for sisyphus framework."
            implementationClass = "com.bybutter.sisyphus.protobuf.gradle.ProtobufPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/ButterCam/sisyphus"
    vcsUrl = "https://github.com/ButterCam/sisyphus"
    description = "Protobuf compiler plugin for sisyphus framework."

    (plugins) {
        "protobuf" {
            tags = listOf("sisyphus", "protobuf", "grpc")
        }
    }
}
