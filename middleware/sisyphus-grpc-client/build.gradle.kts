middleware

plugins {
    `java-library`
    protobuf
}

dependencies {
    api(project(":lib:sisyphus-grpc"))
    implementation(Dependencies.Grpc.stub)
    runtime(Dependencies.nettyTcnative)
}
