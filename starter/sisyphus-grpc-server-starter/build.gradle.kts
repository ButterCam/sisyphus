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
    compileOnly(Dependencies.Spring.Boot.actuator)
    runtimeOnly(Dependencies.Grpc.netty)
    api(Dependencies.Alibaba.Sentinel.GrpcAdapter)
    api(Dependencies.Alibaba.Sentinel.SentinelTransport)
}

protobuf {
    packageMapping(
            "grpc.reflection.v1" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1",
            "grpc.reflection.v1alpha" to "com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha"
    )
}
