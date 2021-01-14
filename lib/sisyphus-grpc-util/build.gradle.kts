lib

plugins {
    antlr
    `java-library`
    protobuf
}

description = "Utils and toolkit for sisyphus customized gRPC runtime"

dependencies {
    api(project(":lib:sisyphus-grpc-coroutine"))

    implementation(project(":lib:sisyphus-jackson"))
    implementation(project(":lib:sisyphus-common"))

    antlr(platform(project(":sisyphus-dependencies")))
    antlr(Dependencies.antlr4)
}
