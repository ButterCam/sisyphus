lib

plugins {
    `java-library`
    protobuf
}

description = "Sisyphus customized Protobuf runtime for Kotlin"

dependencies {
    api(projects.lib.sisyphusCommon)

    proto(libs.protobuf.java)

    testImplementation(libs.junit.jupiter)
    testImplementation(projects.lib.sisyphusGrpc)
    testImplementation(projects.lib.sisyphusGsonProtobuf)
    testImplementation(projects.lib.sisyphusJacksonProtobuf)
}

protobuf {
    packageMapping(
        "com.google.protobuf" to "com.bybutter.sisyphus.protobuf.primitives",
        "com.google.protobuf.compiler" to "com.bybutter.sisyphus.protobuf.compiler"
    )
}
