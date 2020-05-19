starter

plugins {
    `java-library`
}

description = "Starter for configuring HttpMessageEncoder with Jackson in Sisyphus Framework"

dependencies {
    api(project(":lib:sisyphus-jackson"))
    api(Dependencies.Spring.Boot.jackson)
    compileOnly(Dependencies.Jackson.Dataformat.cbor)
    compileOnly(Dependencies.Jackson.Dataformat.smile)
}
