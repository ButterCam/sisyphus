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

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":lib:sisyphus-grpc"))
    testImplementation(project(":lib:sisyphus-gson-protobuf"))
    testImplementation(project(":lib:sisyphus-jackson-protobuf"))
}

protobuf {
    packageMapping(
        "com.google.protobuf" to "com.bybutter.sisyphus.protobuf.primitives",
        "com.google.protobuf.compiler" to "com.bybutter.sisyphus.protobuf.compiler"
    )
}
