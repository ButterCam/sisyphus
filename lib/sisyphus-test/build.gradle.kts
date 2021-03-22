lib

plugins {
    `java-library`
    protobuf
}

description = "Starter for test application in Sisyphus Framework"

dependencies {
    api("org.junit.jupiter:junit-jupiter")

    implementation(project(":lib:sisyphus-jackson-protobuf"))
    implementation(project(":lib:sisyphus-grpc-coroutine"))
    implementation(project(":lib:sisyphus-dsl"))
    implementation("ch.qos.logback:logback-classic")

    testImplementation(project(":starter:sisyphus-grpc-server-starter"))
    testImplementation(Dependencies.Spring.Boot.test)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
