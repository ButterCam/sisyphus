middleware

plugins {
    `java-library`
}

description = "Middleware for using RocketMQ in Sisyphus Project"

dependencies {
    implementation(project(":lib:sisyphus-common"))

    api(project(":middleware:sisyphus-jdbc"))
    implementation(Dependencies.hikari)
    api(Dependencies.seata) {
        exclude("org.springframework", "spring-webmvc")
    }
}
