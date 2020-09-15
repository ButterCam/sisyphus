middleware

plugins {
    `java-library`
}

description = "Starter for building gRPC sentinel in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-jackson"))
    api(project(":lib:sisyphus-grpc"))
    compileOnly(project(":middleware:sisyphus-redis"))
    implementation(Dependencies.Alibaba.Sentinel.GrpcAdapter)
    implementation(Dependencies.Alibaba.Sentinel.SentinelTransport)
    implementation(Dependencies.Alibaba.Sentinel.SentinelFlowControl)
    implementation(Dependencies.Alibaba.Sentinel.SentinelDatasource)
}
