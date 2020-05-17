starter

plugins {
    `java-library`
}

dependencies {
    api(project(":starter:sisyphus-jackson-starter"))
    api(Dependencies.Spring.Boot.webflux)
}
