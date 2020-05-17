lib

plugins {
    `java-library`
    protobuf
}

dependencies {
    implementation(project(":lib:sisyphus-jackson"))
    api(project(":lib:sisyphus-common"))

    implementation(Dependencies.Grpc.proto)
    implementation(Dependencies.Kotlin.Coroutines.guava)
    api(Dependencies.Grpc.stub)
    api(Dependencies.Kotlin.Coroutines.reactor)
    api(Dependencies.Proto.base)

    proto(Dependencies.Proto.runtimeProto)
}

protobuf {
    packageMapping(
        "google.protobuf" to "com.bybutter.sisyphus.protobuf.primitives",
        "google.protobuf.compiler" to "com.bybutter.sisyphus.protobuf.compiler"
    )
}
