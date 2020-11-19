lib

plugins {
    `java-library`
    protobuf
}

description = "Sisyphus customized Protobuf runtime for Kotlin"

dependencies {
    api(project(":lib:sisyphus-common"))
    api(project(":proto:sisyphus-protos"))
    api(Dependencies.Grpc.api)

    proto(platform(project(":sisyphus-dependencies")))
    proto(Dependencies.Proto.base)
    proto(project(":proto:sisyphus-protos"))
}

protobuf {
    packageMapping(
        "google.protobuf" to "com.bybutter.sisyphus.protobuf.primitives",
        "google.protobuf.compiler" to "com.bybutter.sisyphus.protobuf.compiler"
    )
}
