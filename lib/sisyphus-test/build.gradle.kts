lib

plugins {
    `java-library`
    protobuf
}

description = " Test framework for testing gRPC apis in Sisyphus Framework"

dependencies {
    api("org.junit.platform:junit-platform-engine")

    implementation(project(":lib:sisyphus-dsl"))
    implementation(project(":lib:sisyphus-grpc"))
    implementation(project(":lib:sisyphus-jackson-protobuf"))
    implementation("org.junit.platform:junit-platform-launcher")
    implementation("org.reflections:reflections")
    implementation("io.grpc:grpc-stub")

    testImplementation(project(":starter:sisyphus-grpc-server-starter"))
    testImplementation(project(":starter:sisyphus-spring-boot-test-starter"))
}
