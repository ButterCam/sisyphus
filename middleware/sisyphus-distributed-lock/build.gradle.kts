middleware

plugins {
    `java-library`
}

description = "Starter for building distributed lock in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-jackson"))
    compileOnly(project(":middleware:sisyphus-redis"))
}
