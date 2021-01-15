lib

plugins {
    antlr
    `java-library`
    protobuf
}

description = "Sisyphus customized gRPC runtime"

dependencies {
    api(project(":lib:sisyphus-protobuf"))
    api(Dependencies.Grpc.api)

    proto(platform(project(":sisyphus-dependencies")))
    proto(Dependencies.Proto.grpcProto)
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