tools

plugins {
    `java-library`
}

description = "Proto compiler for Sisyphus customized Protobuf runtime"

dependencies {
    api(project(":lib:sisyphus-common"))
    api(Dependencies.Kotlin.poet)
    api(Dependencies.Proto.base)

    implementation(Dependencies.protoc)
    implementation(Dependencies.Proto.grpcProto)

    testImplementation(project(":lib:sisyphus-grpc-coroutine"))
}
