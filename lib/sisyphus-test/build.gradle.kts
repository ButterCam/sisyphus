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
    api("com.github.tomakehurst:wiremock-jre8")
    api("commons-logging:commons-logging")

    testImplementation(project(":starter:sisyphus-grpc-server-starter"))
    testImplementation(Dependencies.Spring.Boot.test)
}