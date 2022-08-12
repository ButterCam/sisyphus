middleware

plugins {
    `java-library`
}

description = "Middleware for using RocketMQ in Sisyphus Project"

dependencies {
    implementation(project(":lib:sisyphus-common"))

    api(Dependencies.rocketMq)
    implementation(Dependencies.rocketMqAcl)
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
}
