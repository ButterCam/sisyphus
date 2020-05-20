middleware

plugins {
    `java-library`
}

description = "Middleware for using Retrofit in Sisyphus Project"

dependencies {
    api(Dependencies.Kotlin.Coroutines.jdk)
    api(Dependencies.retrofit)
    api(Dependencies.okhttp)
    api(Dependencies.reflections)
    api(Dependencies.resilience4j)
    api(project(":lib:sisyphus-common"))
    api(project(":lib:sisyphus-dto"))
    api(project(":lib:sisyphus-jackson"))
}
