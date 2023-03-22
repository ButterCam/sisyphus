plugins {
    `sisyphus-starter`
}

description = "Starter for build application with Protobuf type server in Sisyphus Framework"

dependencies {
    api(projects.lib.sisyphusProtobuf)
    api(projects.lib.sisyphusGrpcCoroutine)
    api(projects.lib.sisyphusCommon)
    api(projects.starter.sisyphusWebfluxStarter)
    implementation(libs.grpc.stub)
}
