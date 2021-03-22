middleware

plugins {
    `java-library`
}

description = "Middleware for using gRPC client in Sisyphus Project"

dependencies {
    api(project(":lib:sisyphus-grpc-coroutine"))
    runtimeOnly(Dependencies.nettyTcnative)
}
