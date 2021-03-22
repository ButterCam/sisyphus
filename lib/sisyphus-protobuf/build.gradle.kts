lib

plugins {
    `java-library`
    protobuf
}

description = "Sisyphus customized Protobuf runtime for Kotlin"

dependencies {
    api(project(":lib:sisyphus-common"))

    proto(platform(project(":sisyphus-dependencies")))
    proto(Dependencies.Proto.base)
}

protobuf {
    packageMapping(
        "com.google.protobuf" to "com.bybutter.sisyphus.protobuf.primitives",
        "com.google.protobuf.compiler" to "com.bybutter.sisyphus.protobuf.compiler"
    )
}
