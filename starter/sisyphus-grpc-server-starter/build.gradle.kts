starter

plugins {
    `java-library`
    protobuf
}

description = "Starter for building gRPC server in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-protobuf"))
    api(project(":middleware:sisyphus-grpc-client"))
    api(project(":middleware:sisyphus-configuration-artifact"))
    implementation(Dependencies.Grpc.stub)
    implementation(Dependencies.Spring.Boot.actuator)
    implementation(Dependencies.Micrometer.prometheus)
    runtimeOnly(Dependencies.Grpc.netty)
}

protobuf {
    packageMapping(
            "grpc.reflection.v1" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1",
            "grpc.reflection.v1alpha" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha"
    )
}
