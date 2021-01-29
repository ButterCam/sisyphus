starter

plugins {
    `java-library`
    protobuf
}

description = "Starter for building gRPC server in Sisyphus Framework"

dependencies {
    api(project(":starter:sisyphus-grpc-server-starter"))
    api(project(":middleware:sisyphus-configuration-artifact"))
    api(project(":lib:sisyphus-jackson"))
    compileOnly(Dependencies.Spring.Boot.actuator)
    runtimeOnly(Dependencies.Grpc.netty)
    implementation(Dependencies.kubeJavaClient)
}