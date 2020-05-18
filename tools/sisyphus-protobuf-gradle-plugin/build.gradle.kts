tools

plugins {
    `java-library`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.1"
}

dependencies {
    implementation(project(":lib:sisyphus-common"))
    implementation(project(":lib:sisyphus-grpc"))
    implementation(project(":lib:sisyphus-jackson"))
    implementation(project(":tools:sisyphus-protoc"))

    implementation(Dependencies.Kotlin.reflect)
    implementation(Dependencies.Kotlin.plugin)
    implementation(Dependencies.Proto.apiCompiler)
}

gradlePlugin {
    plugins {
        create("protobuf") {
            id = "com.bybutter.sisyphus.protobuf"
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
            displayName = "Sisyphus Protobuf Plugin"
            description = "Plugin for compiling proto files in project which based on sisyphus framework."
            tags = listOf("sisyphus", "protobuf", "grpc")
        }
    }
}
