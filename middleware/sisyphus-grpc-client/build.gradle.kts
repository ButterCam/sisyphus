middleware

plugins {
    `java-library`
}

description = "Middleware for using gRPC client in Sisyphus Project"

dependencies {
    api(projects.lib.sisyphusGrpcCoroutine)
    api(libs.spring.boot)
    runtimeOnly(libs.grpc.netty)
}
