starter

plugins {
    `java-library`
}

description = "Starter for build application with Protobuf type server in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-protobuf"))
    api(project(":lib:sisyphus-grpc-coroutine"))
    api(project(":lib:sisyphus-common"))
    api(project(":starter:sisyphus-webflux-starter"))
    implementation(Dependencies.Grpc.stub)
}
