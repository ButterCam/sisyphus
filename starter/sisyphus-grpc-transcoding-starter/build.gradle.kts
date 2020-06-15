starter

plugins {
    `java-library`
}

description = "Starter for building gRPC server which with HTTP and gRPC Transcoding in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-protobuf"))
    api(project(":lib:sisyphus-grpc"))
    api(project(":lib:sisyphus-common"))
    api(project(":middleware:sisyphus-configuration-artifact"))
    api(project(":starter:sisyphus-grpc-server-starter"))
    api(project(":starter:sisyphus-webflux-starter"))
    implementation(Dependencies.Grpc.stub)
    implementation(Dependencies.swagger)
    compileOnly(Dependencies.Spring.Boot.actuator)
}
