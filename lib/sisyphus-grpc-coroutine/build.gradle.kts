lib

plugins {
    `java-library`
    protobuf
}

description = "Sisyphus customized gRPC runtime for Kotlin coroutine(full support)"

dependencies {
    api(project(":lib:sisyphus-grpc"))
    api(Dependencies.Grpc.stub)
    api(Dependencies.Grpc.kotlin)

    proto(platform(project(":sisyphus-dependencies")))
    proto(Dependencies.Proto.grpcProto)
}

protobuf {
    plugins {
        coroutine()
    }
}
