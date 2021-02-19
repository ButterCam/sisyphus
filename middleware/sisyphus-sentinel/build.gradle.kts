middleware

plugins {
    `java-library`
}

description = "Middleware for using Sentinel in Sisyphus Project"

dependencies {
    implementation(project(":lib:sisyphus-common"))

    api(project(":lib:sisyphus-jackson"))
    api(project(":lib:sisyphus-grpc"))
    implementation(project(":middleware:sisyphus-redis"))
    implementation(Dependencies.Alibaba.Sentinel.GrpcAdapter)
    implementation(Dependencies.Alibaba.Sentinel.SentinelTransport)
    implementation(Dependencies.Alibaba.Sentinel.SentinelFlowControl)
    implementation(Dependencies.Alibaba.Sentinel.SentinelDatasource)
}
