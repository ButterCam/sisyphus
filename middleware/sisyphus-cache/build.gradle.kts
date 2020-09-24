middleware

plugins {
    `java-library`
}

description = "Middleware for using cache in Sisyphus Project"

dependencies {
    api(project(":lib:sisyphus-common"))
    api(project(":lib:sisyphus-jackson"))
    api(Dependencies.Spring.Boot.redis)
    api(Dependencies.Kotlin.Coroutines.jdk)
    api(Dependencies.Kotlin.Coroutines.reactive)
}
