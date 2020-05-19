starter

plugins {
    `java-library`
}

description = "Starter for build application with String Webflux in Sisyphus Framework"

dependencies {
    api(project(":starter:sisyphus-jackson-starter"))
    api(Dependencies.Spring.Boot.webflux)
}
