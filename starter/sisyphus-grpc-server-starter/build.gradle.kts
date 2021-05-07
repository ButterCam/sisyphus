starter

plugins {
    `java-library`
    protobuf
}

description = "Starter for building gRPC server in Sisyphus Framework"

dependencies {
    api(project(":middleware:sisyphus-grpc-client"))
    api(project(":middleware:sisyphus-configuration-artifact"))
    implementation("io.grpc:grpc-core")
    compileOnly(Dependencies.Spring.Boot.actuator)
    runtimeOnly(Dependencies.Grpc.netty)
}

protobuf {
    packageMapping(
        "io.grpc.reflection.v1" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1",
        "io.grpc.reflection.v1alpha" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha"
    )
}
