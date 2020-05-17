tools

plugins {
    `java-library`
    `java-gradle-plugin`
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
