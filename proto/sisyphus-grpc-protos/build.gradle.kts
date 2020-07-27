proto

plugins {
    `java-library`
}

description = "Sisyphus common protos for gRPC Runtime"

dependencies {
    api(project(":proto:sisyphus-protos"))
    api(Dependencies.Proto.grpcProto)
}