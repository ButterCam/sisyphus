lib

plugins {
    `java-library`
    protobuf
}

description = "Sisyphus customized gRPC runtime for Kotlin coroutine(full support)"

dependencies {
    api(project(":lib:sisyphus-grpc"))
    api(Dependencies.Grpc.stub)
    api(Dependencies.Grpc.kotlin) {
        exclude("io.grpc", "grpc-protobuf")
    }

    implementation(project(":lib:sisyphus-jackson"))

    proto(project(":lib:sisyphus-grpc"))
}

protobuf {
    plugins {
        separatedCoroutine()
    }
}
