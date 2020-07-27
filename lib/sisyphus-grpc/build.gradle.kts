lib

plugins {
    antlr
    `java-library`
    protobuf
}

description = "Sisyphus customized gRPC runtime for Kotlin coroutine"

dependencies {
    api(project(":lib:sisyphus-protobuf"))
    api(Dependencies.Grpc.stub)
    api(Dependencies.Grpc.kotlin)
    api(Dependencies.Kotlin.Coroutines.reactor)
    api(Dependencies.Kotlin.Coroutines.guava)
    api(Dependencies.Proto.base)
    api(project(":proto:sisyphus-grpc-protos"))

    implementation(project(":lib:sisyphus-jackson"))
    implementation(project(":lib:sisyphus-common"))
    implementation(Dependencies.Grpc.proto)
    implementation(Dependencies.Kotlin.reflect)

    proto(Dependencies.Proto.grpcProto)
    proto(project(":proto:sisyphus-grpc-protos"))
    antlr(Dependencies.antlr4)
}

protobuf {
    packageMapping(
        "google.api" to "com.bybutter.sisyphus.api",
        "google.cloud.audit" to "com.bybutter.sisyphus.cloud.audit",
        "google.geo.type" to "com.bybutter.sisyphus.geo.type",
        "google.logging.type" to "com.bybutter.sisyphus.logging.type",
        "google.longrunning" to "com.bybutter.sisyphus.longrunning",
        "google.rpc" to "com.bybutter.sisyphus.rpc",
        "google.rpc.context" to "com.bybutter.sisyphus.rpc.context",
        "google.type" to "com.bybutter.sisyphus.type"
    )
}
