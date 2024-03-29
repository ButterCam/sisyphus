plugins {
    `sisyphus-middleware`
}

description = "Middleware for using gRPC client in Sisyphus Project"

dependencies {
    api(projects.lib.sisyphusGrpcCoroutine)
    api(libs.spring.boot)
    implementation(projects.middleware.sisyphusSpring)
    runtimeOnly(libs.grpc.netty)
}
