middleware

plugins {
    `java-library`
}

description = "Middleware for testing APIs and services in Sisyphus Project"

dependencies {
    api(Dependencies.Spring.Boot.test)
    api(project(":lib:sisyphus-common"))
    api(project(":lib:sisyphus-jackson"))
    api(project(":middleware:sisyphus-grpc-client"))
}
