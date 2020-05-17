starter

plugins {
    `java-library`
}

dependencies {
    api(project(":lib:sisyphus-protobuf"))
    api(project(":lib:sisyphus-grpc"))
    api(project(":lib:sisyphus-common"))
    api(project(":middleware:sisyphus-configuration-artifact"))
    api(project(":starter:sisyphus-grpc-server-starter"))
    api(project(":starter:sisyphus-webflux-starter"))
    implementation(Dependencies.Grpc.stub)
    implementation(Dependencies.swagger)
}
