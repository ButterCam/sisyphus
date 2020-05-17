middleware

plugins {
    `java-library`
}

dependencies {
    api(project(":lib:sisyphus-common"))
    api(Dependencies.Spring.Boot.redis)
    api(Dependencies.Kotlin.Coroutines.jdk)
}
