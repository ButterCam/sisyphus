tools

plugins {
    `java-library`
}

description = "Proto compiler for Sisyphus customized Protobuf runtime"

dependencies {
    api(project(":lib:sisyphus-common"))
    api(Dependencies.Kotlin.poet)
    api(Dependencies.Proto.base)

    implementation(Dependencies.protoc)
    implementation(Dependencies.Proto.grpcProto)
    implementation("com.google.api:api-common")

    implementation("io.reactivex.rxjava2:rxjava")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":lib:sisyphus-grpc-coroutine"))
    testImplementation(project(":lib:sisyphus-grpc-rxjava"))
}
