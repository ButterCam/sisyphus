middleware

plugins {
    `java-library`
}

description = "Middleware for using MongoDB in Sisyphus Project"

dependencies {
    api(Dependencies.mongo)
    api(Dependencies.Kotlin.Coroutines.reactive)
}
