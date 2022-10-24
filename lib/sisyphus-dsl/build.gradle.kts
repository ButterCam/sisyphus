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
    api("org.antlr:antlr4-runtime")

    antlr(platform(project(":sisyphus-dependencies")))
    antlr("org.antlr:antlr4")

    testImplementation("org.junit.jupiter:junit-jupiter")
}

configurations {
    api.get().setExtendsFrom(api.get().extendsFrom.filter { it.name != "antlr" })
}
