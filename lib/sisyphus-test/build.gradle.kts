plugins {
    `sisyphus-library`
    `sisyphus-protobuf`
}

description = " Test framework for testing gRPC apis in Sisyphus Framework"

dependencies {
    api(libs.junit.jupiter.engine)

    implementation(projects.lib.sisyphusDsl)
    implementation(projects.lib.sisyphusGrpc)
    implementation(projects.lib.sisyphusJacksonProtobuf)
    implementation(libs.junit.launcher)
    implementation(libs.grpc.stub)
    implementation(libs.reflections)

    testImplementation(projects.starter.sisyphusGrpcServerStarter)
    testImplementation(projects.starter.sisyphusSpringBootTestStarter)
}
