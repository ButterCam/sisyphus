plugins {
    `sisyphus-starter`
}

description = "Starter for building gRPC server which with HTTP and gRPC Transcoding in Sisyphus Framework"

dependencies {
    api(projects.lib.sisyphusJacksonProtobuf)
    api(projects.lib.sisyphusGrpcCoroutine)
    api(projects.middleware.sisyphusConfigurationArtifact)
    api(projects.starter.sisyphusGrpcServerStarter)
    api(projects.starter.sisyphusWebfluxStarter)
    implementation(libs.swagger)
    compileOnly(libs.spring.boot.actuator)
}
