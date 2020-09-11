middleware

plugins {
    `java-library`
}

description = "Starter for building gRPC sentinel in Sisyphus Framework"

dependencies {
    api(project(":starter:sisyphus-grpc-server-starter"))
    implementation(Dependencies.Grpc.stub)
    implementation(Dependencies.Alibaba.Sentinel.SentinelFlowControl)
    implementation(Dependencies.Alibaba.Sentinel.SentinelDatasource)
}
