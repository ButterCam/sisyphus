lib

plugins {
    antlr
    `java-library`
    protobuf
}

description = "Utils and toolkit for building gRPC service easier"

dependencies {
    api(project(":lib:sisyphus-grpc"))

    implementation(project(":lib:sisyphus-common"))

    antlr(platform(project(":sisyphus-dependencies")))
    antlr(Dependencies.antlr4)

    testImplementation("org.junit.jupiter:junit-jupiter")
}
