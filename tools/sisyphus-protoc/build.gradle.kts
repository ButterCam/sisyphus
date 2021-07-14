tools

plugins {
    `java-library`
}

description = "Proto compiler for Sisyphus customized Protobuf runtime"

dependencies {
    api(project(":lib:sisyphus-common"))
    api(project(":tools:sisyphus-protoc-runner"))
    api(Dependencies.Kotlin.poet)
    api(Dependencies.Proto.base)

    implementation(Dependencies.Proto.grpcProto)
    implementation("com.google.api:api-common")

    implementation("io.reactivex.rxjava2:rxjava")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":lib:sisyphus-grpc-coroutine"))
    testImplementation(project(":lib:sisyphus-grpc-rxjava"))
}
