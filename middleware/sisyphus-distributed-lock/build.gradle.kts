middleware

plugins {
    `java-library`
}

description = "Starter for building distributed lock in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-jackson"))
    implementation(project(":middleware:sisyphus-redis"))
    implementation(Dependencies.Spring.Boot.aop)
}
