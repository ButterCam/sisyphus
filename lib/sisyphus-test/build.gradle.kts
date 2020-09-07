lib

plugins {
    `java-library`
    protobuf
}

description = "Starter for test application in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-jackson"))
    api(project(":lib:sisyphus-grpc"))
    api("org.junit.jupiter:junit-jupiter-params")
    api("org.junit.jupiter:junit-jupiter")
    api("org.junit.jupiter:junit-jupiter-api")
    api("org.junit.jupiter:junit-jupiter-params")
}