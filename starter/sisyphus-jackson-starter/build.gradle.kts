starter

plugins {
    `java-library`
}

dependencies {
    api(project(":lib:sisyphus-jackson"))
    api(Dependencies.Spring.Boot.jackson)
    compileOnly(Dependencies.Jackson.Dataformat.cbor)
    compileOnly(Dependencies.Jackson.Dataformat.smile)
}
