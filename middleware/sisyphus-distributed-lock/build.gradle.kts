middleware

plugins {
    `java-library`
}

description = "Middleware for building distributed lock in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-jackson"))
    implementation(project(":middleware:sisyphus-redis"))
    implementation(Dependencies.Spring.Boot.aop)
}
