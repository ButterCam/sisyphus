middleware

plugins {
    `java-library`
}

description = "Middleware for using Redis cache in Sisyphus Project"

dependencies {
    api(projects.lib.sisyphusCommon)
    api(libs.spring.boot)
    api(libs.spring.boot.redis)
    api(libs.kotlin.coroutines.jdk)
    api(libs.kotlin.coroutines.reactive)
}
