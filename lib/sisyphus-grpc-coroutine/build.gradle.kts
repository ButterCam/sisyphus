plugins {
    `sisyphus-library`
    `sisyphus-protobuf`
}

description = "Sisyphus customized gRPC runtime for Kotlin coroutine(full support)"

dependencies {
    api(projects.lib.sisyphusGrpc)
    api(libs.kotlin.coroutines)
    api(libs.grpc.stub)
    api(libs.grpc.kotlin) {
        exclude("io.grpc", "grpc-protobuf")
    }

    implementation(projects.lib.sisyphusJackson)

    proto(libs.google.commonProtos)
}

protobuf {
    plugins {
        separatedCoroutine()
    }
}
