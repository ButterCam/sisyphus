middleware

plugins {
    `java-library`
}

description = "Middleware for using Redis cache in Sisyphus Project"

dependencies {
    api(project(":lib:sisyphus-common"))
    api(Dependencies.Spring.Boot.redis)
    api(Dependencies.Kotlin.Coroutines.jdk)
    api(Dependencies.Kotlin.Coroutines.reactive)
}
