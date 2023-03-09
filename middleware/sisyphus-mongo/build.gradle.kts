middleware

plugins {
    `java-library`
}

description = "Middleware for using MongoDB in Sisyphus Project"

dependencies {
    api(libs.spring.boot)
    api(libs.mongodb)
}
