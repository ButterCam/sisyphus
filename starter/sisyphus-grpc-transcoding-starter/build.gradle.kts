starter

plugins {
    `java-library`
}

description = "Starter for building gRPC server which with HTTP and gRPC Transcoding in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-jackson-protobuf"))
    api(project(":lib:sisyphus-grpc-coroutine"))
    api(project(":middleware:sisyphus-configuration-artifact"))
    api(project(":starter:sisyphus-grpc-server-starter"))
    api(project(":starter:sisyphus-webflux-starter"))
    implementation(Dependencies.swagger)
    compileOnly(Dependencies.Spring.Boot.actuator)
}
