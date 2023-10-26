plugins {
    `sisyphus-starter`
    `sisyphus-protobuf`
}

description = "Starter for building gRPC server in Sisyphus Framework"

dependencies {
    api(projects.middleware.sisyphusGrpcClient)
    api(projects.middleware.sisyphusConfigurationArtifact)
    implementation(libs.grpc.core)
    implementation(libs.grpc.inprocess)
    implementation(projects.middleware.sisyphusSpring)

    compileOnly(libs.spring.boot.actuator)
    runtimeOnly(libs.grpc.netty)

    testImplementation(libs.spring.boot.test)
}

protobuf {
    packageMapping(
        "io.grpc.reflection.v1" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1",
        "io.grpc.reflection.v1alpha" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha",
        "io.grpc.health.v1" to "com.bybutter.sisyphus.starter.grpc.support.health.v1"
    )
}
