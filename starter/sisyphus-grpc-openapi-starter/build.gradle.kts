starter

plugins {
    `java-library`
}

description = "Starter for building gRPC server which with HTTP and gRPC Transcoding in Sisyphus Framework"

dependencies {
    implementation(projects.starter.sisyphusGrpcTranscodingStarter)
    api(libs.swagger)
    api(projects.starter.sisyphusWebfluxStarter)
}
